# la-II.analizador-semantico

Analizador Semántico para la clase de Lenguajes y Autómatas II.

El programa lee un archivo fuente en C (`.c`), lo analiza y muestra el resultado en una ventana gráfica dividida verticalmente en dos secciones:

| Sección | Contenido |
|---------|-----------|
| **Tabla de símbolos** (izquierda) | Una fila por cada `Token` reconocido: lexema, tipo, línea, inicio y fin. |
| **Código fuente** (derecha) | El contenido del archivo `.c` que se está leyendo. |

## Requisitos

- JDK 21 o superior
- Maven 3.9+

## Compilar, probar y ejecutar

```bash
mvn compile          # compilar
mvn test             # correr los tests
mvn package          # generar el jar
java -jar target/analizador-semantico-1.0-SNAPSHOT.jar
```

## Estructura del proyecto

```
src/main/java/mx/edu/analizador/
├── Main.java
├── lexico/        # Cursor, Lexer, Token, TipoToken y los reconocedores
├── sintactico/    # analizador sintáctico (pendiente)
└── semantico/     # analizador semántico (pendiente)
src/test/java/...  # tests con JUnit 5, misma estructura de paquetes
```

### Cómo funciona el análisis léxico

- `Cursor` recorre el texto fuente y lleva la línea y la columna (desde 1).
- `Token` es un `record (lexema, tipo, linea, inicio, fin)`. **Cada `Token` es una fila de la tabla de símbolos** que se muestra en la interfaz.
- `TipoToken` enumera las categorías: `PALABRA_RESERVADA`, `IDENTIFICADOR`, `CONSTANTE_ENTERA`, `CONSTANTE_REAL`, `CONSTANTE_CARACTER`, `CADENA`, `OPERADOR`, `SIMBOLO`, `DIRECTIVA`.
- `ReconocedorToken` es la interfaz que implementa cada reconocedor (`puedeIniciar` y `leer`). Las reglas están en el Javadoc de la interfaz.
- `Lexer` recibe una lista de reconocedores y delega cada token al **primer** que aplique, por lo que **el orden de la lista importa** (comentarios antes que operadores, números antes que operadores, etc.).

`ReconocedorSimbolo` es el ejemplo de referencia: úsalo como plantilla para un reconocedor nuevo.

## Cómo contribuir

### 1. Flujo de trabajo

1. Haz fork del repositorio (o clona si tienes acceso) y crea una rama desde `main`. Nómbrala según el equipo o la tarea, por ejemplo `Equipo2` o `feat/reconocedor-numero`.
2. Implementa tu cambio, con tests.
3. Verifica que `mvn test` pase.
4. Abre una Pull Request hacia `main` con un título claro, por ejemplo `Implementación de clase ReconocedorNumero (Equipo 2)`, y una descripción de qué reconoce y qué casos borde cubre.
5. Atiende los comentarios de la revisión con nuevos commits en la misma rama.

### 2. Commits

Usamos mensajes en español con prefijo tipo [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: agregar ReconocedorNumero
fix: clasificar correctamente números con dos puntos decimales
test: casos borde de ReconocedorNumero
chore(setup): configurar .gitignore
```

### 3. Agregar un reconocedor nuevo

1. Crea `lexico/ReconocedorXxx.java` implementando `ReconocedorToken`.
2. Respeta las reglas de la interfaz:
   - `puedeIniciar` **no consume** nada (no llama a `cursor.avanzar()`).
   - `leer` consume exactamente el lexema. `inicio` es `cursor.columna()` **antes** de consumir; `fin` es `cursor.columna() - 1` **después** del último carácter (columnas desde 1, `fin` inclusivo).
   - `leer` puede devolver `null` si lo consumido no genera token (por ejemplo, comentarios).
3. Usa `cursor.siguiente()` / `cursor.siguiente(n)` para mirar adelante sin avanzar.
4. Regístralo en la lista del `Lexer` en la posición correcta.
5. Escribe tests en `src/test/java/mx/edu/analizador/lexico/` (ver `LexerTest` y `CursorTest`). Cubre como mínimo: un caso normal, casos borde, y las columnas `inicio`/`fin`.

### 4. Qué NO subir

- Archivos de IDE: `.idea/`, `*.iml`, `.vscode/`, etc.
- La carpeta `target/` y otros artefactos de compilación.

Si tu IDE genera archivos nuevos, agrégalos al `.gitignore` en lugar de commitearlos.

### 5. Checklist antes de abrir la PR

- [ ] `mvn test` pasa
- [ ] Hay tests para el código nuevo
- [ ] No hay archivos de IDE ni de `target/`
- [ ] El Javadoc describe lo que realmente hace la clase
- [ ] El reconocedor está registrado en el `Lexer` en el orden correcto
- [ ] La descripción de la PR explica los casos borde cubiertos

## Equipo

Proyecto de la materia Lenguajes y Autómatas II.
