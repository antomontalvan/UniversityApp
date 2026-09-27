# UniversityApp — Enciclopedia de Universidades del Mundo

Aplicación Android en Kotlin que busca universidades por país en tiempo real usando la
[Hipolabs Universities API](http://universities.hipolabs.com/search?country=Argentina).

**Video demostrativo:** [VER VIDEO](PEGAR_ACA_EL_LINK_DE_LOOM_YOUTUBE_O_DRIVE)

## Funcionalidades

- Búsqueda en tiempo real con debounce de 500 ms (sin botón, mínimo 3 letras).
- Listado en `RecyclerView` con Adapter y ViewHolder propios.
- Pantalla de detalle (`DetailActivity` + `UniversityDetailFragment`) con dominios web oficiales,
  enlace activo al sitio web, código de país y estado/provincia.
- Manejo de estados: carga (`ProgressBar`), sin resultados, sin conexión y error HTTP, con botón "Reintentar".

## Arquitectura (MVVM + Repository)

```
View (Activity / Fragment)  ──observa──▶  ViewModel (LiveData<UniversityUIState>)
                                              │
                                              ▼
                                        Repository
                                              │
                                              ▼
                                  Retrofit (UniversityService) ──HTTP GET──▶ Hipolabs API
```

| Paquete | Responsabilidad |
|---|---|
| `model` | `University`: data class que mapea el JSON de la API (Serializable). |
| `service` | `UniversityService` (endpoints Retrofit) y `RetrofitClient` (singleton). |
| `repository` | `UniversityRepository`: única fuente de datos; traduce errores HTTP a excepciones. |
| `viewmodel` | `UniversityViewModel` + `UniversityUIState` (estados de la búsqueda) y `DetailViewModel` (compartido entre `DetailActivity` y el Fragment). |
| `ui.main` | `MainActivity` (buscador + lista) y `UniversityAdapter`. |
| `ui.detail` | `DetailActivity` (recibe la universidad por Intent) y `UniversityDetailFragment` (la obtiene con `activityViewModels()`). |

## Tecnologías

Kotlin · MVVM · LiveData · Coroutines · Retrofit + Gson · RecyclerView · Fragments · View Binding · Material Components

## Endpoint utilizado

```
GET http://universities.hipolabs.com/search?country={país}
```

La API sólo responde por HTTP, por lo que se habilitó tráfico cleartext **únicamente** para ese dominio
en `res/xml/network_security_config.xml`.

## Limitaciones conocidas

Los datos de la Hipolabs API no se actualizan con frecuencia: algunos sitios web que devuelve
están caídos, cambiaron de dominio o tienen certificados SSL inválidos. Esto depende de los
servidores de cada universidad, por eso la pantalla de detalle ofrece además un botón
"Buscar en Google" como alternativa.
