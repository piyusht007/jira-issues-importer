# jira-issues-importer

This project intends to import Jira issues using the Jira REST APIs and write the imported issues to a specified output directory.

To import the issues it simply executes the supplied JQL (Jira Query Language) Query. It uses following REST API for that:

```/rest/api/latest/search?jql=<jql_query>```

Jira Server REST APIs Reference: https://docs.atlassian.com/software/jira/docs/api/REST/8.8.1/

To call the REST API, this project uses Basic Authentication (https://developer.atlassian.com/server/jira/platform/basic-authentication/) and requires you to provide a username and password.

There is a limitation when it comes to the maximum number of records we can get from a single API call in Jira. We can fetch only 1000 issues in a single API call. 

To get more records we've to make multiple api calls. This project deals with that using a multi-threaded approach and make multiple api calls using multiple threads. 

Jira REST API response has ```startAt```, ```maxResults``` and ```total``` parameters. Based on the ```total```, api calls can be caculated as per the desired ```maxResults```(batch size). This project uses default batch size of 100 due to the fact that fetching large number of issues results in EOF Exceptions (incomplete response). However a diffrent batch size can be specified as desired.

**Usage:**

``` 
java -jar jira-issues-importer-0.0.1-SNAPSHOT.jar 
     --jira.host=<your_jira_hostname> 
     --jira.search.api.endpoint=/rest/api/latest/search?jql= 
     --output.dir=<path_to_write_issue_files> 
     --username=<username> 
     --password=<password> 
     --batch.size=100 
     --jql=filter=123456 
```
You can add default values for these properties inside ```application.properties``` file or pass them using command line (as shown in above usage) to override the default values as required.