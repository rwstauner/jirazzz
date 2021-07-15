(ns jirazzz.commit-msg-test
  (:require
    [clojure.test :refer [deftest is testing] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]))


(t/use-fixtures :each tu/with-server-reset)


(deftest default
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))
  (tu/respond (:transitions tu/responses))

  (tu/with-temp-files
    [tmp "cmsg.txt"]
    (let [_ (spit tmp "subject\n\nbody\ns'more\n")
          r (jirazzz commit-msg (str tmp)
                     --transition "done"
                     --assignee jzuser)]
      (is (= 0
             (:exit r))
          (:err r))
      (is (= "subject\n\ncloses JZ-123\n\nbody\ns'more"
             (slurp tmp)))
      (is (= "\n"
             (:out r))
          (:err r))))

  (is (= [{:uri (tu/jira-path :meta)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (tu/jira-path :sprint)
           :method "get"}
          {:uri (tu/jira-path :create)
           :method "post"
           :body
           {:fields
            {:summary "subject"
             :description "body\ns'more"
             :project {:id 123456}
             :issuetype {:id 1}
             :assignee {:name "jzuser"}
             :customfield_12345 2
             :customfield_23456 nil}}}
          {:uri (tu/jira-path :transitions)
           :method "get"}
          {:uri (tu/jira-path :transitions)
           :method "post"
           :body
           {:transition
            {:id 3}}}]

         (tu/requests))))

(deftest custom
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))

  (tu/with-temp-files
    [tmp "cmsg.txt"]
    (let [_ (spit tmp "subject\n\nbody\nmore\n")
          r (jirazzz commit-msg (str tmp)
                     --commit-template "[{{issue}}] {{summary}}\n\n{{description}}\n"
                     --assignee jzuser)]
      (is (= 0
             (:exit r))
          (:err r))
      (is (= "[JZ-123] subject\n\nbody\nmore"
             (slurp tmp)))
      (is (= "\n"
             (:out r))
          (:err r))))

  (is (= [{:uri (tu/jira-path :meta)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (tu/jira-path :sprint)
           :method "get"}
          {:uri (tu/jira-path :create)
           :method "post"
           :body
           {:fields
            {:summary "subject"
             :description "body\nmore"
             :project {:id 123456}
             :issuetype {:id 1}
             :assignee {:name "jzuser"}
             :customfield_12345 2
             :customfield_23456 nil}}}]
         (tu/requests))))

(deftest already-there
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:create tu/responses))

  (testing "default"
    (tu/with-temp-files
      [tmp "cmsg.txt"]
      (let [msg "subject\n\ncloses JZ-123\n\nbody\nmore\n"
            _ (spit tmp msg)
            r (jirazzz commit-msg (str tmp)
                       --assignee jzuser)]
        (is (= 0
               (:exit r))
            (:err r))
        (is (= msg
               (slurp tmp)))
        (is (= "\n"
               (:out r))
            (:err r)))))

  (testing "custom"
    (tu/with-temp-files
      [tmp "cmsg.txt"]
      (let [msg "[JZ-123] subject\n\nbody\nmore\n"
            _ (spit tmp msg)
            r (jirazzz commit-msg (str tmp)
                       --issue-pattern "\\[([A-Z]+-[0-9]+)\\]"
                       --assignee jzuser)]
        (is (= 0
               (:exit r))
            (:err r))
        (is (= msg
               (slurp tmp)))
        (is (= "\n"
               (:out r))
            (:err r)))))

  (testing "pattern does not match"
    (tu/with-temp-files
      [tmp "cmsg.txt"]
      (let [_ (spit tmp "subject\n\nrefs JZ-121\n\nbody\nmore\n")
            r (jirazzz commit-msg (str tmp)
                       --issue-type "Story"
                       --issue-pattern "\\[([A-Z]+-[0-9]+)\\]"
                       --commit-template "[{{issue}}] {{summary}}\n\n{{description}}\n"
                       --assignee jzuser)]
        (is (= 0
               (:exit r))
            (:err r))
        (is (= "[JZ-123] subject\n\nrefs JZ-121\n\nbody\nmore"
               (slurp tmp)))
        (is (= "\n"
               (:out r))
            (:err r)))))

  (is (= [{:uri (tu/jira-path :meta)
           :method "get"
           :query-string "projectKeys=JZ"}
          {:uri (tu/jira-path :sprint)
           :method "get"}
          {:uri (tu/jira-path :create)
           :method "post"
           :body
           {:fields
            {:summary "subject"
             :description "refs JZ-121\n\nbody\nmore"
             :project {:id 123456}
             :issuetype {:id 2}
             :assignee {:name "jzuser"}
             :customfield_12345 2
             :customfield_23456 nil}}}]
         (tu/requests))))
