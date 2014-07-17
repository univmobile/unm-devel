# Développement : unm-backend-mock

### Installation sur un poste de développement

Testé avec : 

  * JDK 1.6 
  * Maven 3.0.4
  * Ant 1.8.2
  
Vérifier que le poste a accès aux repositories Nexus suivants :

  * [univmobile.vswip.com/nexus/content/repositories/](univmobile.vswip.com/nexus/content/repositories/)
  * [http://repo.avcompris.net/content/repositories/](http://repo.avcompris.net/content/repositories/)
  
En ligne de commande :

    > git clone https://github.com/univmobile/unm-devel
    > cd unm-backend-mock
    > mvn -U install
    
Note : l’option « -U » (--update-snapshots) permet de forcer la récupération des mises à jour des dépendances snapshots auprès des repositories Nexus, même s’il s’agit du POM parent.
