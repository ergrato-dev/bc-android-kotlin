plugins {
    id("com.android.library")
    id("bitacora.android-base")
}

android {
    // Una biblioteca no tiene targetSdk: lo pone la app. Sin este, Robolectric toma el minSdk de la biblioteca y busca
    // un android-all de esa versión (17), que no está.
    testOptions.targetSdk = 36
}
