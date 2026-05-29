# SGI_PPK
Repositorio del proyecto Sistema Gestor de Inventarios (SGI) desarrollado en Java
En este proyecto se desarrolla un sistema gestor de inventarios basico de momento en Java con la UI de Java Swing, persistencia de datos con JSON y bases de POO

EN futuras actualizaciones se tienen previstos los iguientes cambios:
  - Cambiar Java Swing a JavaFX
  - Cambiar persistencia de datos en JSON a DB
  - Optimizacion de codigo
  - Mejora de funciones
  - Mejor formato en Exel al exportar datos
  - Mejorar eficiencia del proyecto

Para compilar y ejecutar usar las siguientes instrucciones
// ============================= LINUX ==============================

  - mkdir -p out                                        // Crea el directorio de salida
  - javac -d out $(find src -name "*.java") Main.java    // Compila el codigo
  - java -cp out Main                                   // Ejecuta el codigo


// ============================= MacOS ==============================

  - mkdir -p out                                      # Crea el directorio de salida
  - javac -d out $(find . -name "*.java")             # Compila el código (busca desde la raíz)
  - java -cp out Main                                 # Ejecuta el código


// ============================ WINDOWS PowerShell ==============================

  - mkdir -Force out                                  # Crea el directorio de salida
  - javac -d out (dir -Recurse src\*.java, Main.java) # Compila el código de forma recursiva
  - java -cp out Main                                 # Ejecuta el código




// ============================ WINDOWS CMD/Simbolos del Sistema ==============================

if not exist out mkdir out                        # Crea el directorio si no existe
dir /b /s src\*.java Main.java > sources.txt      # Genera la lista de archivos a compilar
javac -d out @sources.txt                         # Compila el código usando la lista
del sources.txt                                   # Limpia el archivo temporal
java -cp out Main                                 # Ejecuta el código




Para probar el proyecto los usuarios son los siguientes
Administrador:
  - Usuario: admin
  - Contraseña: admin123

Vendedor:
  - Usuario: vendedor
  -  Contraseña: vend123
