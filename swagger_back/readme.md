El backend cambio, se dividio en 3 api, dejando a una api con mayor responsabilidad con los temas de autorizacion autenticacion, edugo-api-iam-platform, pero siguen estando las api tambien, mande hacer un analisis de la situacion actual, contra. la situacion que se debe cambiar en los archivos
* informe_adaptacion_backend.md
* informe_flujo_actual.md

Es un pequeño analisis como abre boca, por supuesto tu puedes indagar mucho mas tanto en los archivos swagger que esta en esta carpeta como en las nuevsa direcioniones de las api

/Users/jhoanmedina/source/EduGo/EduBack/edugo-api-iam-platform
/Users/jhoanmedina/source/EduGo/EduBack/edugo-api-admin-new
/Users/jhoanmedina/source/EduGo/EduBack/edugo-api-mobile-new

La api edugo-api-iam-platform es la nueva centralizadora para tema de autenticacion y demas labores administrativa

La api edugo-api-admin-new es la encargada de la parte de administracion pero del negocio

La api edugo-api-mobile-new es la encargada de los procesos, que mas se consume al dia a dia


Tambien recordar que tenemos el proyecto compartidos, donde esta la responsabilidades comunes

/Users/jhoanmedina/source/EduGo/EduBack/edugo-infrastructure
/Users/jhoanmedina/source/EduGo/EduBack/edugo-shared
/Users/jhoanmedina/source/EduGo/EduBack/edugo-dev-environment

donde edugo-infrastructure es la responsable de la estructura de la base de datos y datos de migracion, en tema de base de datos
El proyecto edugo-shared son modulos de condigo comunes al ecosistema

EL proyecto edugo-dev-environment, es para crear el ambiente, como la conexiones a la base de datos como Neon

La base de datos esta en 

    "DATABASE_POSTGRES_HOST": "ep-green-frost-ado4abbi-pooler.c-2.us-east-1.aws.neon.tech",
    "DATABASE_POSTGRES_PORT": "5432",
    "DATABASE_POSTGRES_USER": "neondb_owner",
    "DATABASE_POSTGRES_PASSWORD": "npg_sC2u9pTVwQJI",
    "DATABASE_POSTGRES_DATABASE": "edugo",
    "DATABASE_POSTGRES_SSL_MODE": "require",


Quiero que despues de este analisis proceda a crear un equipo de agentes para migrar lo necesario con la nueva tecnologia, dando prioridad en

1) manejo de variable de ambiente con la nueva url de la nueva api:
    a) Crear la nueva variable para la nueva api
    b) actualizar las rutas de las apis, local, que cambiaron
        b.1) http://localhost:8070/. -- Para edugo-api-iam-platform
        b.2) http://localhost:8060/. -- Para edugo-api-admin-new
        b.3) http://localhost:8065/. -- Para edugo-api-mobile-new
2) Adaptar lo referente a login y refresh token a las nuevas direccion
    a) puedes correr las apis, para que consuma los curl para validar si asi lo quieres las respuesta o mas informacion que te pueda dar los logs corriendo, o cosas que no tenga el swagger.json
    b) para correr cada api, en cada carpeta de la api, dentro de la carperta .zed y el archivo debug.json, puedes usar la configuracion "Go: Debug main (CLOUD MODE - Neon/Atlas)", para levantar  y que necesita
3) Cargar el menu con el nuevo esquema de navegacion segun el endpoint
    a) adaptar el esquema segun el tipo de plataforma
    b) recordar que las prioridades son
        b.1) Android Telefono (iphone sera el mismo, no hay diferencia)
        b.2) Android Tablet
        b.3) Escritorio FHD
        b.4) Web, estandar material Designer 3.0
    c) Me interesa evaluar primero el menu, mas alla de las ventanas dinamicas
    d) el flujo deberia ser login/ splash -> menu -> ventanas dummy
4) Manejo de UI
    a) En segunda fase cuando hallamos probado los 3 puntos anteriores adaptamos el tema de la UI dinamica
    b) Pero dejemos documentados solo los pasos que necesitamos para crear la nueva adaptacion.
    c) en esta primera corrida, el proyecto debe levantar hasta el menu  autenticado y documetnos de plan de trabajo para migrar la UI dinamica, que cambio 

