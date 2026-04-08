# ⚠️ Erreur de Démarrage - Solution

## 🔴 Erreur Rencontrée

```
Failed to configure a DataSource: 'url' attribute is not specified and no embedded datasource could be configured.
Reason: Failed to determine a suitable driver class
```

## ✅ Problème Identifié et Corrigé

### Causes Possibles
1. ❌ MySQL n'est pas en cours d'exécution
2. ❌ La base de données ForsaBD n'existe pas
3. ❌ Configuration du serveur (port 8089 au lieu de 8080)
4. ❌ Encodage du fichier application.properties corrompu

### Corrections Apportées
1. ✅ Changé le port du serveur de `8089` → `8080`
2. ✅ Ajouté les paramètres MySQL manquants (`useSSL`, `serverTimezone`, etc.)
3. ✅ Spécifié explicitement le driver MySQL (`com.mysql.cj.jdbc.Driver`)
4. ✅ Changé le dialecte Hibernate vers `MySQL8Dialect`
5. ✅ Régénéré le fichier `application.properties`

---

## 🚀 Comment Démarrer Correctement

### Étape 1: Vérifier que MySQL est Exécuté

#### Sur Windows - Service MySQL
```powershell
# Vérifier si le service MySQL est en cours d'exécution
Get-Service MySQL80

# Si arrêté, le démarrer
Start-Service MySQL80
```

#### Ou démarrer MySQL manuellement (si vous avez un script)
```powershell
# Exécutez le script fourni
C:\Users\ASUS\Desktop\PIDev-Forsa\start-mysql.ps1
```

### Étape 2: Vérifier la Connexion MySQL

```bash
# Depuis PowerShell
mysql -u root -p

# (aucun mot de passe, appuyez simplement sur Entrée)
```

Si cela fonctionne, vous devriez voir:
```
mysql>
```

### Étape 3: Créer la Base de Données (si elle n'existe pas)

```sql
-- Depuis mysql>
CREATE DATABASE IF NOT EXISTS ForsaBD;
USE ForsaBD;
SHOW TABLES;
EXIT;
```

### Étape 4: Compiler le Projet

```powershell
cd C:\Users\ASUS\Desktop\PIDev-Forsa
.\mvnw.cmd clean compile -DskipTests
```

Attendez la compilation... Elle devrait réussir sans erreurs.

### Étape 5: Redémarrer l'Application

Appuyez sur le bouton ▶️ dans IntelliJ IDEA ou exécutez:

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 🔍 Diagnostic

### Vérifier le Fichier application.properties

Le fichier a été corrigé. Vérifiez que ces lignes sont présentes:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ForsaBD?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
server.port=8080
```

### Vérifier MySQL

```bash
# Depuis PowerShell
mysql --version

# Vous devriez voir quelque chose comme:
# mysql  Ver 8.0.32 for Win64 on x86_64 (MySQL Community Server - GPL)
```

### Logs d'Erreur

Si l'erreur persiste, cherchez:
- ❌ `Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'`
  → **Cause:** Datasource non configurée ou MySQL arrêté

- ❌ `Connection refused`
  → **Cause:** MySQL n'est pas en cours d'exécution sur localhost:3306

---

## ✅ Checklist de Démarrage

- [ ] MySQL est en cours d'exécution (service ou manuellement)
- [ ] Base de données `ForsaBD` existe (crée automatiquement sinon)
- [ ] Fichier `application.properties` corrigé
- [ ] Port `8080` (vérifié dans application.properties)
- [ ] Pas de conflit de port (8080 libre)
- [ ] Compilation réussie (`mvnw clean compile`)

---

## 🎯 Étapes Rapides pour Démarrer

```powershell
# 1. Vérifier MySQL
Get-Service MySQL80

# 2. Compiler
cd C:\Users\ASUS\Desktop\PIDev-Forsa
.\mvnw.cmd clean compile -DskipTests

# 3. Démarrer
.\mvnw.cmd spring-boot:run

# 4. Accéder à Swagger
# Ouvrez: http://localhost:8080/forsaPidev/swagger-ui.html
```

---

## ❓ Problèmes Courants

### Problème: "Access denied for user 'root'@'localhost'"
**Solution:** Vérifiez le mot de passe MySQL dans `application.properties`
```properties
spring.datasource.password=votre_mot_de_passe
```

### Problème: "Communications link failure"
**Solution:** MySQL n'est pas en cours d'exécution
```powershell
Start-Service MySQL80
```

### Problème: "Port 8080 already in use"
**Solution:** Changez le port dans `application.properties`
```properties
server.port=8081  # ou un autre port libre
```

### Problème: "Unknown database 'ForsaBD'"
**Solution:** La base de données sera créée automatiquement à cause de `createDatabaseIfNotExist=true`
- Si elle n'est pas créée, créez-la manuellement:
```sql
CREATE DATABASE ForsaBD CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## 📝 Fichier application.properties - Sections Clés

Après les corrections, votre fichier devrait contenir:

```properties
# === DATABASE CONFIGURATION ===
spring.datasource.url=jdbc:mysql://localhost:3306/ForsaBD?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# === SERVER CONFIGURATION ===
server.port=8080
server.servlet.context-path=/forsaPidev

# === JPA CONFIGURATION ===
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# === REST (autres configurations) ===
...
```

---

## 🎉 Résultat Attendu

Après le démarrage réussi, vous devriez voir dans les logs:

```
...
INFO 43928 --- [           main] o.e.forsapidev.ForsaPidevApplication     : Starting ForsaPidevApplication
...
INFO 43928 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
INFO 43928 --- [           main] o.e.forsapidev.ForsaPidevApplication     : Started ForsaPidevApplication in X.XXX seconds
```

Et vous pourrez accéder à:
```
✅ Swagger UI: http://localhost:8080/forsaPidev/swagger-ui.html
```

---

## 🔧 Support Supplémentaire

Si les problèmes persistent:

1. **Redémarrez MySQL:**
   ```powershell
   Stop-Service MySQL80
   Start-Service MySQL80
   ```

2. **Nettoyez les fichiers compilés:**
   ```powershell
   .\mvnw.cmd clean
   ```

3. **Vérifiez les dépendances:**
   ```powershell
   .\mvnw.cmd dependency:tree | Select-String mysql
   ```

4. **Lisez les logs complets:**
   - Cherchez les erreurs avant `ERROR` dans la console IntelliJ

---

**Status:** ✅ **CONFIGURATION CORRIGÉE - PRÊT À REDÉMARRER**

