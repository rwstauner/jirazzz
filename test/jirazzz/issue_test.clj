(ns jirazzz.issue-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest edn
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:issue tu/responses))

  (let [r (jirazzz issue "JZ-123")]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "{:just \"print it\"}\n"
           (:out r))
        (:err r)))

  ; TODO: no need for get-meta
  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (:sprint tu/jira-paths)
           :method "get"}
          {:uri (:issue tu/jira-paths)
           :method "get"}]
         (tu/requests))))

(deftest json
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:issue tu/responses))

  (let [r (jirazzz issue "JZ-123"
                   --output-format json)]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "{\"just\":\"print it\"}\n"
           (:out r))
        (:err r)))

  ; TODO: no need for get-meta
  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (:sprint tu/jira-paths)
           :method "get"}
          {:uri (:issue tu/jira-paths)
           :method "get"}]
         (tu/requests))))