# Login - Distribucion Estructural

## Mobile (Android/iOS)

Pantalla completa, single-column, centrado vertical.

```
PANTALLA
├── Scroll: vertical
├── Alineacion: centrado horizontal
├── Padding: horizontal medio
│
├── ZONA: Brand
│   ├── Posicion: parte superior, centrado
│   ├── Titulo app (texto grande, bold)
│   └── Subtitulo / tagline (texto pequeno)
│
├── Espaciado grande
│
├── ZONA: Formulario
│   ├── Distribucion: stacked (columna)
│   ├── Campo email (text-input, full-width, keyboard=email)
│   ├── Espaciado
│   ├── Campo password (password-input, full-width, toggle visibility)
│   ├── Espaciado
│   ├── Fila auxiliar (side-by-side):
│   │   ├── Izquierda: checkbox "Recordarme"
│   │   └── Derecha: text-button "Olvidaste tu contrasena?"
│   ├── Espaciado
│   └── Boton principal "Iniciar Sesion" (filled, full-width)
│
├── Espaciado
│
├── ZONA: Separador social
│   ├── Divider con texto central "o continuar con"
│   └── Fila de botones sociales (side-by-side, outlined, iconos)
│
├── Espaciado
│
└── ZONA: Registro
    └── Texto + link "No tienes cuenta? Registrate" (centrado)
```

## Desktop

Split horizontal: panel de marca a la izquierda, formulario a la derecha.

```
PANTALLA
├── Distribucion: side-by-side (row)
│
├── ZONA: Brand Panel [weight=0.4, fondo=primaryContainer]
│   ├── Alineacion: centrado vertical y horizontal
│   ├── Padding: grande
│   ├── Titulo app (texto extra-grande)
│   ├── Tagline (titulo medio)
│   └── Descripcion (body, centrado)
│
└── ZONA: Formulario Panel [weight=0.6]
    ├── Alineacion: centrado vertical y horizontal
    ├── Padding: grande
    │
    ├── Titulo "Iniciar Sesion" (headline)
    ├── Espaciado
    ├── Campo email (text-input, full-width)
    ├── Espaciado
    ├── Campo password (password-input, full-width)
    ├── Espaciado
    ├── Fila auxiliar (side-by-side):
    │   ├── checkbox "Recordarme"
    │   └── text-button "Olvidaste tu contrasena?"
    ├── Espaciado
    ├── Boton "Iniciar Sesion" (filled, full-width)
    ├── Divider + botones sociales
    └── Link registro
```

## Web

Card centrada con ancho maximo, scroll vertical.

```
PANTALLA
├── Fondo: surface
├── Alineacion: centrado total (horizontal + vertical)
├── Scroll: vertical
│
└── ZONA: Card elevada [max-width=480dp, centrada]
    ├── Padding: grande
    ├── Distribucion: stacked (columna, centrado)
    │
    ├── Brand (titulo + subtitulo, centrado)
    ├── Espaciado
    ├── Campo email (full-width)
    ├── Campo password (full-width)
    ├── Fila: checkbox + link
    ├── Boton principal (full-width)
    ├── Divider + social
    └── Link registro
```

## Modelo Abstracto de Plantilla

```json
{
  "pattern": "login",
  "zones": [
    {
      "id": "brand",
      "type": "container",
      "position": "top",
      "alignment": "center",
      "slots": [
        {"id": "app_title", "controlType": "label", "style": "headline"},
        {"id": "tagline", "controlType": "label", "style": "body"}
      ]
    },
    {
      "id": "form",
      "type": "form-group",
      "position": "center",
      "distribution": "stacked",
      "slots": [
        {"id": "email", "controlType": "text-input", "keyboard": "email", "width": "full"},
        {"id": "password", "controlType": "password-input", "width": "full"},
        {
          "id": "aux_row",
          "type": "container",
          "distribution": "side-by-side",
          "slots": [
            {"id": "remember_me", "controlType": "checkbox"},
            {"id": "forgot_password", "controlType": "text-button", "alignment": "end"}
          ]
        },
        {"id": "submit", "controlType": "filled-button", "width": "full"}
      ]
    },
    {
      "id": "social",
      "type": "container",
      "distribution": "stacked",
      "slots": [
        {"id": "divider", "controlType": "divider-with-text"},
        {
          "id": "social_buttons",
          "type": "container",
          "distribution": "side-by-side",
          "slots": [
            {"id": "google", "controlType": "outlined-button", "icon": "google"},
            {"id": "apple", "controlType": "outlined-button", "icon": "apple"}
          ]
        }
      ]
    },
    {
      "id": "signup_link",
      "type": "container",
      "alignment": "center",
      "slots": [
        {"id": "signup", "controlType": "text-button-link"}
      ]
    }
  ],
  "platformOverrides": {
    "desktop": {
      "rootDistribution": "side-by-side",
      "zones": {
        "brand": {"weight": 0.4, "background": "primaryContainer", "alignment": "center-both"},
        "form": {"weight": 0.6, "alignment": "center-both"}
      }
    },
    "web": {
      "rootDistribution": "centered-card",
      "rootMaxWidth": 480,
      "zones": {}
    }
  }
}
```
