(ns jirazzz.test-util
  (:require
    [babashka.fs :as fs]
    [cheshire.core :as json]
    [clojure.java.shell :refer [sh]]
    [org.httpkit.client :as http]))



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
