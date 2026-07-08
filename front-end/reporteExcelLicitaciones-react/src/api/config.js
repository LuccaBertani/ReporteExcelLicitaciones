// URL base del back-end. Todas las llamadas a la API parten de acá.
//
// En local/Docker usa localhost:8080 por defecto. En Railway (o cualquier otra
// nube) el backend vive en su propio dominio, así que se configura vía la
// variable de entorno VITE_API_BASE_URL al momento del build (Vite incrusta
// este valor en el bundle final, no se puede cambiar después en runtime).
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
