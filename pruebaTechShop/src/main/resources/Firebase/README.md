# Credenciales de Firebase

El archivo `techshop-key.json` **no está en el repositorio** a propósito: contiene la
llave privada de la cuenta de servicio de Firebase y subirla equivale a publicar la
contraseña del Storage.

## Cómo obtenerlo

1. Entrar a la [consola de Firebase](https://console.firebase.google.com/) y elegir el proyecto.
2. ⚙️ **Configuración del proyecto** → pestaña **Cuentas de servicio**.
3. **Generar nueva clave privada** → se descarga un `.json`.
4. Guardar ese archivo en esta carpeta con el nombre exacto `techshop-key.json`.

El nombre y la ruta se configuran en `application.properties`:

```properties
firebase.json.path= firebase
firebase.json.file= techshop-key.json
```

## Si falta el archivo

La aplicación **no arranca**: `StorageConfig` lo busca en el classpath al crear el bean
`Storage` y falla con `FileNotFoundException`.

## Si da "Invalid JWT Signature"

Significa que la llave fue rotada o revocada en Google Cloud. Hay que generar una nueva
siguiendo los pasos de arriba.
