(ns jirazzz.test-util
  (:require
    [babashka.fs :as fs]
    [cheshire.core :as json]
    [clojure.java.shell :refer [sh]]
    [org.httpkit.client :as http]
    [selmer.parser :as selmer]))


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
  (concat [sh bb script]
          (map #(cond-> % (simple-symbol? %) str) args)
          [:env {"JIRAZZZ_CONFIG_FILE" `*test-config*
                 "JIRAZZZ_TEST_URL" url}]))


(def jira-paths
  {:assignee "/rest/api/2/issue/{{issue}}/assignee"
   :comment "/rest/api/2/issue/{{issue}}/comment"
   :create "/rest/api/2/issue"
   :issue "/rest/api/2/issue/{{issue}}"
   :meta "/rest/api/2/issue/createmeta"
   :search "/rest/api/2/search"
   :sprint "/rest/greenhopper/1.0/sprintquery/{{rapid-view}}"
   :transitions "/rest/api/2/issue/{{issue}}/transitions"})

(defn jira-path
  [k & [vars]]
  (selmer/render
    (get jira-paths k)
    (merge {:rapid-view 98
            :issue "JZ-123"}
           vars)))

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
           :body (pr-str opts)))


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
  {:assignee
   {:match {:uri (jira-path :assignee)
            :body (json/generate-string
                    {:name "jzuser"})}
    :status 204
    :body ""}

   :assignee-unknown
   {:match {:uri (jira-path :assignee)
            :body (json/generate-string
                    {:name "bad-user"})}
    :headers {"content-type" "application/json"}
    :status 400
    :body (json/generate-string
            {:errors {:assignee "User 'bad-user' does not exist."}})}

   :create
   {:match {:uri (jira-path :create)}
    :status 204
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:key "JZ-123"})}

   :comment
   {:match {:uri (jira-path :comment)
            :body (json/generate-string
                    {:body "one more thing"})}
    :status 204
    :body ""}

   :issue
   {:match {:uri (jira-path :issue)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:just "print it"})}

   :meta
   {:match {:uri (jira-path :meta)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:projects
             [{:key "JZ"
               :id 123456
               :issuetypes
               [{:id 1 :name "Task"}
                {:id 2 :name "Story"}]}]})}
   :search
   {:match {:uri (jira-path :search)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:issues [{:key "ABC-234"
                       :fields {:status {:name "idk"} :summary "sum"}}]})}
   :sprint
   {:match {:uri (jira-path :sprint)}
    :status 200
    :headers {"content-type" "application/json"}
    :body (json/generate-string
            {:sprints
             [{:id 0 :state "CLOSED"}
              {:id 1 :name "no" :state "ACTIVE"}
              {:id 2 :name "yes" :state "ACTIVE"}
              {:id 3 :state "FUTURE?"}]})}
   :transitions
   {:match {:uri (jira-path :transitions)}
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

(defmacro with-temp-files
  [binds & body]
  `(let ~(->> binds
              (partition 2)
              (mapcat (fn [[bind suffix]]
                        [bind
                         (list 'java.io.File/createTempFile "jirazzz." suffix)]))
              vec)
     (try
       ~@body
       (finally
         (doseq [tmp# ~(vec (take-nth 2 binds))]
           (~'.delete tmp#))))))
