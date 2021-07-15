(ns jirazzz.test-util
  (:require
    [babashka.fs :as fs]
    [cheshire.core :as json]
    [clojure.java.shell :refer [sh]]
    [org.httpkit.client :as http]))


(def bb (-> (java.lang.ProcessHandle/current) .info .command .get))


(def root
  (-> *file*
      fs/parent
      fs/parent
      fs/parent))


(defn file
  [& args]
  (str (apply fs/path root args)))


(def script (file "jirazzz"))

(def url "http://localhost:54646")

(def ^:dynamic *test-config*
  (file "test" "config.edn"))


(defmacro with-test-config
  [path & body]
  `(binding [*test-config* (file "test" ~path)]
     ~@body))


(defmacro jirazzz
  [& args]
  (->> (concat [bb script]
               (map #(cond-> % (simple-symbol? %) str) args)
               [:env {"JIRAZZZ_CONFIG_FILE" `*test-config*
                      "JIRAZZZ_TEST_URL" url}])
       (apply list sh)))


(def jira-paths
  {:create "/rest/api/2/issue"
   :meta "/rest/api/2/issue/createmeta"
   :sprint "/rest/greenhopper/1.0/sprintquery/98"
   :transitions "/rest/api/2/issue/JZ-123/transitions"})


(defn request
  [method path & kv]
  @(http/request (merge {:as :text
                         :method method
                         :url (str url path)}
                        (apply hash-map kv))))


(defn respond
  [opts]
  (request :post "/respond"
           :headers {"content-type" "application/json"}
           :body (json/generate-string opts)))


(defn parse-body
  [req]
  (cond-> req
    (= "{"
       (some-> req
               :body
               (subs 0 1)))
    (update :body #(json/parse-string % true))))


(defn requests
  []
  (-> (request :get "/requests")
      :body
      (json/parse-string true)
      :requests
      (->> (map parse-body))))


(def responses
  {:create
   {:match {:uri (:create jira-paths)}
    :status 204
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:key "JZ-123"})}
   :meta
   {:match {:uri (:meta jira-paths)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:projects
             [{:key "JZ"
               :id 123456
               :issuetypes
               [{:id 1 :name "Task"}
                {:id 2 :name "Story"}]}]})}
   :sprint
   {:match {:uri (:sprint jira-paths)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:sprints
             [{:id 1 :state "CLOSED"}
              {:id 2 :state "ACTIVE"}
              {:id 3 :state "FUTURE?"}]})}
   :transitions
   {:match {:uri (:transitions jira-paths)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:transitions
             [{:id 1 :name "To Do"}
              {:id 2 :name "Ready For Review"}
              {:id 3 :name "Done"}]})}})


(defn reset
  []
  (request :post "/reset"))


(defn with-server-reset
  [f]
  (reset)
  (f))
