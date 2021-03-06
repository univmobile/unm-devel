unm-devel-sysadmin
==================

_Fichiers système qui aident au développement du projet UnivMobile_

Documentation parente : [unm-devel](../README.md "Documentation parente : unm-devel/README.md")

### Sauvegarde de la configuration de Jenkins
 
Dans src/main/jenkins/config/, les éléments historisés de la configuration de Jenkins. On y retrouve :
 
  * la configuration générale, les nodes (config.xml)
  * les jobs (jobs/)
  * les utilisateurs (users/)

### Scripts d’intégration continue hors Jenkins

En raison des contraintes Apple liées à la sécurité (keychains), nous n’avons pas réussi à faire passer la ligne de commande Mac OS X « xcodebuild test » (tests XCTest) hors d’un environnement auquel nous sommes déjà connectés. En conséquence, il n’est pas possible de la lancer en SSH dans un node Jenkins à partir d’un master ; nous sommes obligés de la gérer à la main et de laisser tourner une tâche régulière.

Voici la solution mise en place :

  1. Lancement toutes les 5 minutes d’une tâche en local sur Mac OS X.
  2. Cette tâche scrute le repository GitHub unm-ios. S’il y a eu des changements :
  3. Lancement des tests XCTest en local.
  4. Enregistrement des résultats des tests XCTest dans le repository GitHub unm-integration.
  5. Un projet Java unm-ios-ut-results, en job piloté par Jenkins, qui scrute le repository GitHub unm-integration. S’il y a eu des changements :
  6. Création à la volée de tests JUnit (@RunWith(Parameterized.class)) qui transforment en résultats « Java + Jenkins » les derniers résultats non tagués des derniers tests XCTest. 
  7. Le job Jenkins est alors en succès / échec selon que les tests XCTest passés en ligne de commande étaient en succès / échec.
  7. Pose dans le repository GitHub unm-integration d’un tag pour indiquer que ces résultats de tests XCTest ont été traités.
  
Techniquement, on a les éléments suivants :

  * pseudocron.sh, dans unm-devel-sysadmin, responsable de lancer une tâche et d’attendre 5 minutes entre deux lancements : il lance macos_job-xcodebuild_test.sh.
  * macos_job-xcodebuild_test.sh, dans unm-devel-sysadmin, responsable de vérifier les modifications dans GitHub du repository unm-ios et de lancer un script s’il y en a eu : il lance xcodebuild_test.sh.
  * xcodebuild_test.sh, dans unm-ios, responsable de :
      * s’appuyer sur l’environnement de sécurité local,
      * lancer les tests XCTest par « xcodebuild test » en ligne de commande,
      * enregistrer les résultats dans un fichier xcodebuild_test.log, dans unm-ios-ut-results (repository GitHub unm-integration).
  * unm-ios-ut-results, projet Java + Maven, dans unm-integration, et lancé par Jenkins, responsable de :
      * scruter les modifications de xcodebuild_test.log dans repository GitHub unm-integration,
      * transformer ces résultats en tests Java, que Jenkins lance et dont il affiche les résultats,
      * poser un tag dans le repository unm-integration.
      
Le suivi des commits GitHub est très important :

  * les tests XCTest sont lancés à partir du code Objective-C ayant un certain commitId dans unm-ios, appelons-le appCommitId, car il correspond au code de l’application iOS.
  * les résultats de ces tests XCTest sont enregistrés dans xcodebuild_test.log, avec un certain commitId dans unm-integration, appelons-le logCommitId, car il correspond au fichier de log.
  * Le job Jenkins unm-ios-ut-results va lui-même se lancer dans un certain commitId du repository unm-integration (runCommitId), tout en ne s’appliquant qu’au dernier commitId non tagué de xcodebuild_test.log (logCommitId).
  * Le tag sera posé sur le commit de xcodebuild_test.log (logCommitId).

Le fichier de log xcodebuild_test.log référence le commitId du code Objective-C (appCommitId).

Le tag dans unm-integration sur le commit \<logCommitId\> est de la forme suivante : 

    processedTestResults/<logCommitId>
  
Le message du tag contient le commit (appCommitId) du code Objective-C sur lequel les tests XCTest ont été passés :

    $ cd ~/unm-integration
    $ git show -s processedTestResults/00d86b2d114981c8fdc9063d5aa92af27d4c9559
    tag processedTestResults/00d86b2d114981c8fdc9063d5aa92af27d4c9559
    Tagger: jenkins <jenkins@unm-ci.avcompris.net>
    Date:   Fri Jul 11 07:47:17 2014 +0200
    Set by UnivMobileTest.java
    commit 00d86b2d114981c8fdc9063d5aa92af27d4c9559
    Date:   Fri Jul 11 07:46:46 2014 +0200
  
        xcodebuild test, git commit: 3172256e9eb0b268080bd28b352cbc86c009321a

    $ cd ~/unm-ios
    $ git show -s 3172256e9eb0b268080bd28b352cbc86c009321a
    commit 3172256e9eb0b268080bd28b352cbc86c009321a
    Date:   Fri Jul 11 01:31:36 2014 +0200

        Debug: Fix: Use __FILE__ to locate the src/test/json/ directory

Dans cet exemple, logCommitId=00d86b2d114981c8fdc9063d5aa92af27d4c9559 (repository unm-integration), et appCommitId=3172256e9eb0b268080bd28b352cbc86c009321a (repositiry unm-ios).

Il faut lancer le job Jenkins unm-ios-ut-results régulièrement afin de ne sauter aucune modification de xcodebuild_test.log ; on atteint ce résultat en laissant Jenkins scruter les modifications.

Un autre projet Java unm-ios-ut-results_up_to_date, lui aussi lancé toutes les 5 minutes par Jenkins, vérifie au cas où que tous les commits du fichier xcodebuild_test.log (logCommitIds) ont été traités par unm-iot-ut-results, c’est-à-dire qu’un tag git a été posé. En cas contraire (unm-ios-ut-results_up_to_date en échec), on peut alors déclencher à la main unm-ios-ut-results dans Jenkins. Néanmoins, le cas est rare, le projet unm-ios-ut-results_up_to_date sert essentiellement au monitoring.

### Signature de l’archive Android (.apk)

Le job Android-UnivMobile crée deux archives :

 * UnivMobile-debug.apk
 * UnivMobile-release.apk — signée
 
L’archive UnivMobile-release.apk est signée avec la clef du Google Play Store.

Afin de ne stocker ni la clef ni les mots de passe dans la configuration Jenkins, vu qu’elle est enregistrée régulièrement dans src/main/jenkins/config/ dans GitHub, et que des éléments seraient également visibles dans les logs des builds, la solution suivante a été adoptée.

Un fichier « keystore.properties », local à l’environnement d’exécution de Jenkins, et contenant les lignes suivantes :

    key.store=/path/to/univmobile.keystore
    key.store.password=xxx
    key.alias=xxx
    key.alias.password=xxx

Ensuite, le job Android-UnivMobile dans Jenkins est configuré pour lancer Ant avec la target : release -propertyfile /local/path/to/keystore.properties

### Cargo + Tomcat  + MySQL
 
Les projets Maven qui héritent d’unm-backend-it-parent ont en commun 
de déployer l’application web unm-backend.war
— en fait unm-backend-app-noshib, c’est-à-dire avec le filtre
qui simule la présence de Shibboleth —
dans un serveur Tomcat local
démarré par Cargo.

Ce sont les projets suivants :

  * unm-backend-it
  * unm-ios-it
  * unm-android-it
  * unm-mobileweb-it
  
En plus de fichiers XML de données locaux,
l’application web J2EE unm-backend a besoin d’une DataSource MySQL
pour l’indexation des données,
il faut donc déclarer cette DataSource dans le Tomcat local.

Pour la description générale
des différents points de configurations :
[J2EE.md](https://github.com/univmobile/unm-backend/blob/develop/J2EE.md)

En intégration continue, pour que la DataSource soit bien utilisable pendant les tests
déployés, il faut :

  * que le \<container/\> Cargo « tomcat7x » soit déclaré en tant 
    que \<type\>installed\</type\> dans pom.xml
  * qu’un fichier local au projet src/test/conf/context.xml contienne la ligne \<ResourceLink/\> adéquate (voir [J2EE.md](https://github.com/univmobile/unm-backend/blob/develop/J2EE.md))
  * que ce fichier soit copié dans le répertoire conf/ de Tomcat
  * que l’installation locale de Tomcat, qui servira de base à Cargo,
    contienne le driver JDBC
    (exemple : mysql-connector-java-5.1.32-bin.jar)
    dans lib/
  * que l’installation locale de Tomcat
    contienne aussi
    la déclaration \<Resource/\> de la DataSource dans conf/server.xml (ce fichier sera copié
    par défaut Cargo, contrairement à conf/context.xml)
  * que le web.xml de la webapp contienne une ligne \<resource-ref/\> correspondant.

Ne pas oublier de déclarer les properties suivantes pour le Maven profile actif :

  * mysql.url
  * mysql.username
  * mysql.password.ref (expression XPath dans pom.xml qui pointe vers un mot de
    passe dans ~/.m2/settings.xml)
  