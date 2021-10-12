(ns jirazzz.comment-test
  (:require
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest add-comment
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:comment tu/responses))

  (let [r (jirazzz comment
                   --issue "JZ-123"
                   --comment "one more thing")]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "Comment added to JZ-123\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (tu/jira-path :comment)
           :method "post"
           :body {:body "one more thing"}}]
         (tu/requests))))
