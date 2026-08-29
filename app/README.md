# MedTracker

Aplicación Android para registrar tomas de medicamentos, consultar el historial, calcular una frecuencia aproximada de consumo y realizar copias de seguridad mediante archivos JSON.

## Estado del proyecto

**Versión prevista:** 1.0.0  
**Estado:** MVP funcional  
**Paquete Android:** `com.berto.medtracker`  
**Lenguaje:** Kotlin  
**Interfaz:** Jetpack Compose con Material 3  
**Persistencia:** SQLite mediante `SQLiteOpenHelper`  
**SDK mínimo:** Android Oreo

La aplicación dispone de tres pantallas principales:

- **Añadir:** creación de registros.
- **Registros:** consulta, filtrado, edición y borrado.
- **Config:** gestión de medicamentos e importación/exportación.

La navegación se realiza mediante una barra inferior fija construida con Navigation Compose.

---

## Funcionalidades implementadas

### Añadir

Permite crear un registro con los siguientes campos:

- `Med`
- `Dosis`
- `Efectos`
- `FechaToma`
- `FechaRegistro`
- `Info`

Comportamiento:

- `Med` se selecciona desde un desplegable alimentado por la tabla `meds`.
- `Dosis` es obligatoria.
- `Efectos` e `Info` son opcionales.
- La fecha de toma se inicializa con la fecha y hora actuales.
- El usuario puede modificar la fecha mediante los selectores nativos de fecha y hora de Android.
- `FechaRegistro` se obtiene automáticamente al guardar.
- Tras guardar, se limpian los campos y se restablece la fecha actual.
- La lista de medicamentos se refresca automáticamente al volver desde Config.

### Registros

Permite:

- Seleccionar un medicamento.
- Mostrar la fecha de toma más reciente.
- Calcular el intervalo medio entre tomas consecutivas.
- Filtrar registros mediante las fechas `Desde` y `Hasta`.
- Mostrar automáticamente los resultados cuando cambia cualquier filtro.
- Abrir un registro pulsando su tarjeta.
- Editar `Med`, `Dosis`, `FechaToma` e `Info`.
- Borrar un registro.
- Solicitar confirmación antes de borrar.

Valores iniciales del filtro:

- `Med`: primer medicamento disponible.
- `Desde`: fecha actual menos un mes.
- `Hasta`: fecha actual.

Cada tarjeta del listado muestra:

- Fecha de toma.
- Dosis.
- Información adicional.

El cálculo de frecuencia utiliza la media de los intervalos entre las fechas de toma. Si hay menos de dos registros válidos, se muestra que no existen datos suficientes.

### Config

Permite gestionar los medicamentos:

- Añadir un medicamento.
- Evitar duplicados sin distinguir mayúsculas y minúsculas.
- Seleccionar un medicamento desde un desplegable.
- Borrar un medicamento.
- Impedir el borrado si existen registros asociados.

También permite gestionar copias de seguridad:

- Exportar todos los medicamentos y registros a JSON.
- Compartir el JSON mediante el selector nativo de Android.
- Importar un JSON mediante el selector de documentos.
- Sustituir completamente los datos actuales durante la importación.
- Solicitar confirmación antes de iniciar una importación destructiva.

No se solicitan permisos amplios de almacenamiento. La exportación usa un fichero temporal compartido mediante `FileProvider`, y la importación usa `ActivityResultContracts.OpenDocument`.

---

## Modelo de dominio

Ruta:

```text
app/src/main/java/com/berto/medtracker/domain/model/Entry.kt
```

Estructura conceptual:

```kotlin
data class Entry(
    val id: Long = 0,
    val med: String,
    val dosis: String,
    val efectos: String,
    val fechaToma: LocalDateTime,
    val fechaRegistro: LocalDateTime,
    val info: String
)
```

El identificador `id` es generado automáticamente por SQLite.

---

## Base de datos

### Implementación

Ruta principal:

```text
app/src/main/java/com/berto/medtracker/data/local/MedTrackerDbHelper.kt
```

La base de datos se llama:

```text
medtracker.db
```

La implementación usa `SQLiteOpenHelper`.

### Tabla `entries`

```text
id                INTEGER PRIMARY KEY AUTOINCREMENT
med               TEXT NOT NULL
dosis             TEXT NOT NULL
efectos           TEXT NOT NULL
fecha_toma        TEXT NOT NULL
fecha_registro    TEXT NOT NULL
info              TEXT NOT NULL
```

Las fechas se guardan como texto en formato ISO local, por ejemplo:

```text
2026-08-29T21:45:00
```

### Tabla `meds`

```text
id      INTEGER PRIMARY KEY AUTOINCREMENT
name    TEXT NOT NULL UNIQUE COLLATE NOCASE
```

`COLLATE NOCASE` evita duplicados como `Paracetamol` y `paracetamol`.

---

## Repositorio

Ruta:

```text
app/src/main/java/com/berto/medtracker/data/repository/EntryRepository.kt
```

Responsabilidades principales:

```text
insertEntry(entry)
getAllEntries()
updateEntry(entry)
deleteEntry(entryId)
deleteAllEntries()
addMed(name)
getAllMeds()
canDeleteMed(name)
deleteMed(name)
replaceAllData(meds, entries)
```

Las operaciones de base de datos se ejecutan desde corrutinas con `Dispatchers.IO`.

`replaceAllData()` debe ejecutarse dentro de una transacción:

1. Borra los registros actuales.
2. Borra los medicamentos actuales.
3. Inserta los medicamentos importados.
4. Inserta los registros importados.
5. Confirma la transacción solo si no ha surgido ningún error.

---

## Copias de seguridad

Ruta:

```text
app/src/main/java/com/berto/medtracker/data/backup/BackupService.kt
```

### Formato JSON

Ejemplo:

```json
{
  "version": 1,
  "exportedAt": "2026-08-29T21:45:00",
  "meds": [
    "Paracetamol",
    "Ibuprofeno"
  ],
  "entries": [
    {
      "med": "Paracetamol",
      "dosis": "500 mg",
      "efectos": "Dolor reducido",
      "fechaToma": "2026-08-29T20:00:00",
      "fechaRegistro": "2026-08-29T20:01:10",
      "info": "Después de cenar"
    }
  ]
}
```

Los identificadores no se exportan. Al importar, SQLite genera identificadores nuevos.

### Exportación

El archivo temporal se crea en:

```text
cache/exports/medtracker-backup.json
```

Se comparte mediante:

```text
Intent.ACTION_SEND
```

El `Uri` se proporciona con `FileProvider` y acceso temporal de lectura.

### Importación

La importación:

- Lee un `Uri` proporcionado por el selector del sistema.
- Valida que el contenido pueda interpretarse como JSON.
- Reconstruye medicamentos y registros.
- Añade a `meds` los medicamentos encontrados en registros si faltan en la lista exportada.
- Sustituye la información actual mediante `replaceAllData()`.

Antes de abrir el selector se presenta una confirmación que advierte de que se sustituirán todos los datos.

---

## Navegación

Rutas:

```text
app/src/main/java/com/berto/medtracker/ui/navigation/AppRoutes.kt
```

```text
add
entries
config
```

Implementación:

```text
app/src/main/java/com/berto/medtracker/ui/navigation/AppNavigation.kt
```

La pantalla inicial es `Añadir`.

La barra inferior contiene únicamente las etiquetas:

```text
Añadir | Registros | Config
```

---

## Pantallas y componentes

```text
app/src/main/java/com/berto/medtracker/ui/
├── components/
│   └── ScreenHeader.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── AppRoutes.kt
├── screens/
│   ├── addentry/
│   │   └── AddEntryScreen.kt
│   ├── entries/
│   │   └── EntriesScreen.kt
│   └── config/
│       └── ConfigScreen.kt
└── theme/
    └── MedTrackerTheme.kt
```

---

## Tema visual

Ruta:

```text
app/src/main/java/com/berto/medtracker/ui/theme/MedTrackerTheme.kt
```

La aplicación sigue el modo claro u oscuro del sistema mediante:

```kotlin
isSystemInDarkTheme()
```

Color principal:

```text
#E91E63
```

El componente reutilizable `ScreenHeader` muestra títulos centrados con título y subtítulo.

Encabezados utilizados:

```text
Añadir
Registros
Config
```

---

## Consideraciones de privacidad

MedTracker registra información relacionada con medicamentos.

- Los datos se almacenan localmente.
- La copia JSON no está cifrada.
- El usuario decide a qué aplicación o servicio comparte la copia.
- Se debe evitar compartir el JSON en canales públicos.
- Desinstalar la aplicación elimina normalmente la base de datos local.
- Conviene exportar una copia antes de desinstalar o borrar los datos de la aplicación.

---

## Limitaciones conocidas

- Las fechas están almacenadas como `LocalDateTime`, sin zona horaria explícita.
- La importación sustituye todos los datos, no fusiona copias.
- No existe cifrado de la base de datos ni del JSON.
- No existen recordatorios o notificaciones.
- No hay pruebas automatizadas todavía.
- La capa de estado se gestiona directamente en composables y repositorio, sin ViewModel dedicado.

---

## Mejoras futuras posibles

1. Añadir pruebas unitarias para cálculo de frecuencia e importación JSON.
2. Añadir ViewModels para separar estado de pantalla y acceso a datos.
3. Migrar a Room si aumenta la complejidad del esquema.
4. Añadir exportación con nombre fechado.
5. Añadir fusión opcional durante la importación.
6. Cifrar las copias de seguridad.
7. Añadir recordatorios configurables.
8. Añadir búsqueda global y filtros adicionales.
9. Añadir una pantalla Acerca de con versión y advertencia de uso.
10. Mejorar accesibilidad, tamaños táctiles y descripciones semánticas.

