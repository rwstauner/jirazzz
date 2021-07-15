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

(def test-config (file "test" "config.edn"))


(defmacro jirazzz
  [& args]
  (->> (concat [bb script] args [:env {"JIRAZZZ_CONFIG_FILE" test-config}])
       (map (fn [s] (if (symbol? s) (str s) s)))
       (apply list sh)))

(def jira-paths
  {:meta "/rest/api/2/issue/createmeta"})


(def url "http://localhost:54646")


(defn read-body
  [b]
  (when b
    (if (string? b)
      b
      (slurp b))))


(defn request
  [method path & kv]
  @(http/request (merge {:method method
                         :url (str url path)}
                        (apply hash-map kv))))


(defn respond
  [opts]
  (request :post "/respond"
           :headers {"content-type" "application/json"}
           :body (json/generate-string opts)))


(defn requests
  []
  (-> (request :get "/requests")
      :body
      (json/parse-string true)
      :requests))


(defn reset
  []
  (request :post "/reset"))


(defn with-server-reset
  [f]
  (reset)
  (f))
