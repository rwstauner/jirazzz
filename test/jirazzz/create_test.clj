(ns jirazzz.create-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest minimal
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))

  (let [r (tu/with-test-config
            "none"
            (jirazzz create
                     --url "http://localhost:54646"
                     --project "JZ"
                     --issue-type "Task"
                     --summary "hi there"
                     --description "meh"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (:meta tu/jira-paths)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (:create tu/jira-paths)
           :method "post"
           :body
           {:fields
            {:summary "hi there"
             :description "meh"
             :project {:id 123456}
             :issuetype {:id 1}
             :assignee {:name "-1"}}}}]
         (tu/requests))))
