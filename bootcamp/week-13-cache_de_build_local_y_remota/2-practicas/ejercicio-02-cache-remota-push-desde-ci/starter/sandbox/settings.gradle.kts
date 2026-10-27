rootProject.name = "bitacora"

// Paso 1: lee la URL de la caché remota de la propiedad de Gradle `bitacora.cacheUrl` (providers.gradleProperty).
//         Sin URL, no hay caché remota: el build funciona solo con la local.
// Paso 2: con URL, declara buildCache { remote<HttpBuildCache> { ... } } con esa url.
//         Solo el CI sube resultados: isPush es true únicamente si existe la variable de entorno CI.
// Paso 3: las credenciales salen de las variables de entorno CACHE_USER y CACHE_PASSWORD.
//         Nunca escritas en este archivo: está en el repositorio y lo lee cualquiera.
