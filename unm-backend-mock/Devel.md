# Développement : unm-backend-mock

Documentation parente : [README.md](README.md "Documentation parente : unm-backend-mock/README.md")

### Installation sur un poste de développement

Testé avec : 

  * JDK 1.6 
  * Maven 3.0.4
  * Ant 1.8.2
  
Vérifier que le poste a accès aux repositories Nexus suivants :

  * [http://univmobile.vswip.com/nexus/content/repositories/](http://univmobile.vswip.com/nexus/content/repositories/)
  * [http://repo.avcompris.net/content/repositories/](http://repo.avcompris.net/content/repositories/)
  
En ligne de commande :

    > git clone https://github.com/univmobile/unm-devel
    > cd unm-backend-mock
    > mvn -U install
    
Note : l’option « -U » (--update-snapshots) permet de forcer la récupération des mises à jour des dépendances snapshots auprès des repositories Nexus, même s’il s’agit du POM parent.

Pour permettre à Eclipse d’ouvrir le projet, utiliser Maven pour produire
le fichier « .project » :

    > mvn eclipse:eclipse

puis importer le projet dans Eclipse.

Autre méthode : ouvrir le projet dans Eclipse à l’aide des plugins adéquats.