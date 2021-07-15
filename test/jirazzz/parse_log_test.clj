(ns jirazzz.parse-log-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest is] :as t]
    [jirazzz.test-util :refer [jirazzz] :as tu]
    [selmer.filters :as sf]
    [selmer.parser :refer [render]]))


(t/use-fixtures :each tu/with-server-reset)

(sf/add-filter! :indent
                (fn [s n]
                  (string/replace
                    s
                    #"(?m:^)"
                    (apply str (repeat (Integer/parseInt n) " ")))))

(defn git-log
  [& msgs]
  (let [template "commit {{sha}}\nAuthor: {{author}}\nDate:   {{date}}\n\n{{message|indent:4}}\n"]
    (-> msgs
        (->> (map #(render template
                           {:sha "x"
                            :author "y"
                            :date "d"
                            :message %}))
             (string/join "\n")))))


(deftest none
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:transitions tu/responses))

  (let [r (jirazzz parse-log
                   --transition "done"
                   --assignee jzuser
                   :in (git-log "foo" "bar\nbaz"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "\n"
           (:out r))
        (:err r)))

  (is (= []
         (tu/requests))
      "no requests"))

(deftest issues
  (tu/respond (:meta tu/responses))
  (tu/respond (:sprint tu/responses))
  (tu/respond (:transitions tu/responses))
  (tu/respond (-> (:transitions tu/responses)
                  (assoc-in [:match :uri] (tu/jira-path :transitions {:issue "JZ-124"}))))

  (let [r (jirazzz parse-log
                   --transition "done"
                   --assignee jzuser
                   :in (git-log "foo\n\ncloses JZ-123" "bar\nbaz" "none" "[JZ-124] subject\n\ndesc"))]
    (is (= 0
           (:exit r))
        (:err r))
    (is (= "JZ-123 transitioned to done\nJZ-124 transitioned to done\n"
           (:out r))
        (:err r)))

  (is (= [{:uri (tu/jira-path :transitions)
           :method "get"}
          {:uri (tu/jira-path :transitions)
           :method "post"
           :body
           {:transition
            {:id 3}}}
          {:uri (tu/jira-path :transitions {:issue "JZ-124"})
           :method "get"}
          {:uri (tu/jira-path :transitions {:issue "JZ-124"})
           :method "post"
           :body
           {:transition
            {:id 3}}}]

         (tu/requests))))
