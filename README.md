# Advanced Search Taxonomy Workflow Plugin

The Advanced Search Taxonomy Workflow plugin provides CMS users an ability to simultaneously set multiple documents with 
selected taxonomy categories using the Advanced Search perspective.

Assuming that taxonomy is enabled to one or more document types, you can filter those documents you want to change, and 
set them altogether with your selected taxonomy categories.

The CMS administrator can configure the taxonomy name to populate categories.

# Documentation (Local)

The documentation is generated by this command:

 > mvn clean site

The output is in the target/site/ directory by default. You can open target/site/index.html in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://bloomreach-forge.github.io/advanced-search-taxonomy-workflow/](https://bloomreach-forge.github.io/advanced-search-taxonomy-workflow/)

You can generate the GitHub pages only from master branch by this command:

 > mvn clean install
 > find docs -name "*.html" -exec rm {} \;
 > mvn -Pgithub.pages clean site

The output is in the docs/ directory by default. You can open docs/index.html in a browser.

You can push it and GitHub Pages will be served for the site automatically. 
