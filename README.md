# jirazzz

A jira rest client command line app written as a [babashka][babashka] script.

Designed for configurability, to be usable with any jira instance.

## Authentication

### OAuth token

Jira Cloud: Go to https://id.atlassian.com/manage/api-tokens and create a new token. Then put its base64-encoded value in the config (see below).

### Cookie

Maybe you can't configure cli authentication, or don't want to use basic auth...
Just visit jira in your browser and steal
your own cookie (see "Example Config" below).

## Installation

Install Babashka. Then, Inside a directory that is on your path, such as `/usr/local/bin`:

```
wget https://raw.githubusercontent.com/rwstauner/jirazzz/main/jirazzz
chmod u+x jirazzz
wget https://raw.githubusercontent.com/rwstauner/jirazzz/main/example-config.edn -O ~/.jirazzz.edn
```

Finally, adjust `~/.jirazzz.edn` as necessary.

## Usage

    jirazzz --help

<!-- { jirazzz help -->

    Usage: jirazzz command [options]

    Commands:

      assign        Set assignee on issue: jirazzz assign --issue ABC-123 username
      comment       Add comment to issue: jirazzz comment --issue ABC-123 --comment 'Something to say'
      commit-msg    Create issue from commit msg if no reference exists
      create        Create issue: jirazzz create --summary … --description …
      example-alias Custom alias command
      issue         Get issue: jirazzz issue KEY
      mine          List open issues assigned to me
      parse-log     Parse commit log and transition referenced issues
      transition    Transition issue: jirazzz transition --issue ABC-123 --transition 'done'
      get           Send GET    request: jirazzz get    /rest/api/x/y/z …
      post          Send POST   request: jirazzz post   /rest/api/x/y/z …
      put           Send PUT    request: jirazzz put    /rest/api/x/y/z …
      delete        Send DELETE request: jirazzz delete /rest/api/x/y/z …

    Options can be specified on the command line or in example-config.edn
    Any aliases defined in {:custom-fields {:your-name :customfield_00000}}
    can also be set in the config file or on the command line (see below).

    Options:
      -h, --help                      Show usage
          --assignee USERNAME         Assign issue to username (-1 for unassigned)
          --backlog                   Put isue in backlog instead of current sprint
          --comment BODY              Comment body to add to issue
          --commit-template TEMPLATE  Selmer template for adding issue to commit message
          --description DESC          Issue description
      -i, --issue KEY                 Issue Key
          --issue-pattern PATTERN     Regular Expression to find issue in commit message
          --issue-type TYPE           Issue Type
      -p, --project PROJECT           Project for issue creation
          --rapid-view ID             RapidView id
          --sprint-pattern PATTERN    Regular Expression to find current sprint
          --summary SUMMARY           Issue summary
          --transition NAME           Transition issue to this state
          --unassigned                Set issue as Unassigned
          --url URL                   Jira URL
          --input-format FORMAT       Set input format (can be 'json', default is 'edn')
          --output-format FORMAT      Set output format (can be 'json', default is 'edn')
          --sprint-id VALUE           Set custom field for sprint-id (customfield_12345)
          --storypoints VALUE         Set custom field for storypoints (customfield_10001)


<!-- jirazzz help } -->


## Configuration

The default file is `~/.jirazzz.edn`.
An alternate can be specified with env var `JIRAZZZ_CONFIG_FILE`.

<!-- { jirazzz example-config -->

```clojure
{
 ; URL prefix of your jira instance.
 :url "https://jira.example.com"
 ; Any headers you want to add to requests (useful for auth).
 :headers {"Cookie" #file ".cookie.txt"}
 ; or, with an oauth token:
 ;:headers {"Authorization" "Basic <base64-encoded-token-value>"}
 ; The rapid-view / sprint board id.
 :rapid-view 123
 ; Project key.
 :project "ABC"
 ; Jira username (might be something like "flastname").
 :assignee #env USER
 ; Type of issue to create.
 :issue-type "Task"
 ; Pattern to look for in commit messages.  Should have single capture group.
 :issue-pattern "\\[([A-Z]+-[0-9]+)\\]"
 ; A selmer template for how to add issue to commit messages.
 :commit-template "[{{issue}}] {{summary}}\n\n{{description}}"
 ; Transition issue to this state.
 :transition "ready for review"
 ; Pattern to match against sprint names when looking for current (first ACTIVE) sprint.
 :sprint-pattern "."
 ; Values for defined custom fields.
 :storypoints 1
 ; Aliases for custom fields so they are recognized by config/arguments.
 :custom-fields
 {
  ; Sprint-id will be filled in with "current sprint".
  :sprint-id   :customfield_12345
  ; Other fields can be specified on the command line or in config.
  :storypoints :customfield_10001
 }
 :aliases
 {example-alias {:doc "Custom alias command"
                 :args ["search" "--jql" "project = ABC AND statusCategory IN (…)"]}}
}
```

<!-- jirazzz example-config } -->

Most fields in the config file can be overridden via command line arguments
(see usage output).

Available readers:
- `#env` - Substitute an environment variable
- `#file` - Read file (relative to config file directory)

Need to figure out your custom fields?

Look at the json output for an existing issue
by inspecting the response in your browser, or use jirazzz:

    jirazzz issue ABC-123


## Pronunciation

It's a combination of "jira" and "💤" (it's a "rest" client).
I call it "jira Z's".

## Development

Try `bb tasks` to see a list of babashka tasks useful for development:

<!-- { jirazzz bb tasks -->

    The following tasks are available:

    docs        Regenerate docs
    lint
    nrepl       Start nrepl server for editor integration
    pre-commit  A git pre-commit hook to check docs, style, etc
    server      Run fake http server for manual testing
    style:check
    style:fix
    test        Run test suite with fake http server


<!-- jirazzz bb tasks } -->



## License

Copyright © 2021 Randy Stauner

Distributed under the Eclipse Public License version 1.0.


[babashka]: https://babashka.org
