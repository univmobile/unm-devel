unm-backend-mock
================

_Webapp bouchon qui sert des flux JSON en dur_

Webapp bouchon pour servir des flux JSON
aux applications UnivMobile iOS et Android
en cours de développement.

Documentation parente : [unm-devel](../README.md "Documentation parente : unm-devel/README.md")

Autres documentations :

  * [Développement : unm-backend-mock](Devel.md "Documentation : unm-backend-mock/Devel.md")

### Manuel d’utilisation

![](src/site/resources/images/backend-mock.png?raw=true =500x "Screenshot")

Se connecter à la webapp déployée en développpement, en général :
[http://univmobile.vswip.com/unm-backend-mock/](http://univmobile.vswip.com/unm-backend-mock/)

Pour obtenir l’adresse d’un flux JSON en dur : 

  * cliquer sur un lien bleu « JSON ».

Pour modifier un flux JSON en dur :

  * cliquer sur son libellé, par exemple « regions »
  * son libellé (par exemple, « regions ») doit s’afficher dans la zone « Path: » et son contenu dans 
  la zone « JSON Content: »
  * modifier le texte dans la zone « JSON Content: »
  * cliquer sur « Save »

Pour créer un flux JSON en dur :
  
  * saisir un nouveau libellé dans « Path: »
  * saisir un nouveau contenu dans « JSON Content: »
  * cliquer sur « Save »
  * l’URL du nouveaux flus JSON s’obtient en cliquant sur son lien « JSON »
  
### Liens

  * Accès à la webapp : [http://univmobile.vswip.com/unm-backend-mock/](http://univmobile.vswip.com/unm-backend-mock/)
  * GitHub : [unm-backend-mock](https://github.com/univmobile/unm-devel/tree/develop/unm-backend-mock "Projet unm-backend-mock dans GitHub")
  * Jenkins Job : [http://univmobile.vswip.com/job/unm-backend-mock/](http://univmobile.vswip.com/job/unm-backend-mock/)
  * [Maven Generated Site](http://univmobile.vswip.com/nexus/content/sites/pub/unm-backend-mock/0.0.4/ "Maven Generated Site: unm-backend-mock:0.0.4")
