(ns jirazzz.search-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest search
  (tu/respond (:meta tu/responses))
  (tu/respond (:search tu/responses))

  (let [r (jirazzz search "a and b")]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= [["" "key" "status" "summary" "browse url"]
            ["" "ABC-234" "idk" "sum" "http://localhost:54646/browse/ABC-234"]]
           (-> (:out r)
               string/split-lines
               (->> (filter (complement string/blank?))
                    (remove #(re-matches #"[-+|]+$" %))
                    (map #(-> %
                              (string/split #"\|")
                              (->> (map string/trim)))))))
        (:err r)))

  (is (= [{:uri (tu/jira-path :search)
           :query-string "jql=a+and+b"
           :method "get"}]
         (tu/requests))))
