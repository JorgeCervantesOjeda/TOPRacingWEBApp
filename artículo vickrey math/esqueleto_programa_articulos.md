# Programa de artículos sobre subasta, eficiencia y equilibrio inducido por incentivos

## Visión general

El programa puede organizarse como una secuencia de cinco artículos conectados pero conceptualmente distinguibles. El primer artículo fija el mecanismo básico. El segundo estudia el equilibrio de decisiones inducido por ese mecanismo. El tercero analiza el aprendizaje informacional y la calidad de señal del mercado. El cuarto extrae implicaciones institucionales sobre libertad tecnológica y diseño reglamentario. El quinto valida y explora el sistema mediante simulaciones y casos numéricos.

La ventaja de esta separación es que permite mantener cada artículo dentro de un foco analítico claro, evitando mezclar en un mismo texto fundamentos microeconómicos, teoría de juegos, diseño institucional y exploración computacional.

---

## Artículo 1. Subasta Vickrey, precio descubierto y eficiencia monetaria

### Pregunta central

¿Cómo puede una subasta de segundo precio producir una señal pública de mercado utilizable para medir desempeño relativo en un entorno competitivo?

### Tesis

Cuando las valoraciones privadas se forman a partir de señales observables interpretadas subjetivamente, una subasta Vickrey posterior al evento puede producir un precio descubierto que funcione como referencia pública para construir una medida de eficiencia monetaria ex post.

### Resumen abstracto

El artículo formaliza un mecanismo compuesto en el que el desempeño observado del vehículo es interpretado privadamente por los ofertantes, traducido en pujas dentro de una subasta Vickrey y transformado, al cierre del mecanismo, en un precio descubierto. Ese precio se emplea como señal pública de mercado para estimar una expectativa de desempeño y, a partir de ella, una medida de eficiencia monetaria. El objetivo no es agotar la teoría institucional del sistema completo, sino mostrar la coherencia estructural entre valoración privada, mercado y premio reglamentario.

### Esqueleto sugerido

1. Introducción
2. Notación y arquitectura abstracta del mecanismo
3. Valoración privada subjetiva del vehículo
4. Dinámica de ajuste de pujas
5. Regla institucional de subasta y precio descubierto
6. Eficiencia monetaria como residual respecto del mercado
7. Implementación operativa del premio
8. Discusión conceptual
9. Conclusiones

### Núcleo formal

- función de valoración privada;
- regla de ajuste de pujas;
- regla abstracta de subasta y especialización Vickrey;
- función de expectativa de mercado;
- definición de eficiencia monetaria;
- separación entre plano privado y plano reglamentario.

### Lo que debe quedar fuera

- teoría completa del equilibrio de inversión en tecnología y pilotaje;
- implicaciones amplias sobre reglamento técnico;
- simulaciones extensas.

---

## Artículo 2. Equilibrio de decisiones bajo premio por eficiencia monetaria

### Pregunta central

¿Hacia qué tipo de decisiones de inversión, contratación y puja conduce un sistema en el que el premio depende del desempeño relativo al precio descubierto por subasta?

### Tesis

Bajo supuestos adecuados, el sistema induce un equilibrio en el que los agentes orientan su conducta hacia tecnologías con mayor desempeño marginal por unidad de costo, hacia pilotos con mayor contribución esperada neta de salario y hacia pujas alineadas con la valoración privada dentro del subjuego de subasta.

### Resumen abstracto

El artículo modela al participante como un agente que decide simultáneamente cuánto invertir en tecnología, qué calidad de pilotaje contratar y cómo valorar activos dentro de un mercado secundario revelado por subasta. La hipótesis central es que el premio por eficiencia monetaria transforma el problema competitivo: ya no se trata de maximizar velocidad bruta, sino de maximizar desempeño relativo al costo total y al valor de mercado descubierto. El resultado esperado es un punto de equilibrio en el que gasto, desempeño, precio esperado y premio esperado se vuelven mutuamente consistentes.

### Esqueleto sugerido

1. Introducción y relación con el mecanismo básico
2. Variables estratégicas de largo plazo
3. Función objetivo del participante
4. Inversión tecnológica y condición marginal de eficiencia
5. Contratación de pilotaje y aporte neto esperado
6. Subjuego de subasta y sinceridad de puja
7. Equilibrio del sistema de incentivos
8. Implicaciones económicas
9. Conclusiones

### Núcleo formal

- utilidad esperada del participante;
- costos de tecnología y salarios de pilotaje;
- premio esperado y valor de reventa esperado;
- condiciones de primer orden o desigualdades de optimalidad;
- definición de equilibrio o punto fijo del sistema.

### Proposiciones naturales

- la inversión óptima en tecnología iguala beneficio marginal esperado y costo marginal;
- la contratación óptima de pilotaje iguala aporte marginal esperado y salario marginal;
- la presión del premio reduce la ventaja automática del gasto bruto;
- en el subjuego Vickrey, la puja coincide con la valoración privada bajo supuestos estándar.

### Lo que debe quedar fuera

- toda la casuística institucional del ecosistema;
- reglamento técnico detallado;
- análisis empírico exhaustivo.

---

## Artículo 3. Información, aprendizaje y calidad de señal del mercado

### Pregunta central

¿Cómo modifica este sistema los incentivos para estudiar tecnología, pilotaje y mercado, y cómo afecta eso a la calidad informativa del precio descubierto?

### Tesis

El mecanismo no solo asigna vehículos y determina premios, sino que también incentiva a participantes y observadores a mejorar sus capacidades de valoración. Esa mejora de valoración eleva la calidad informativa del precio descubierto y, con ello, la fidelidad de la medición de eficiencia monetaria.

### Resumen abstracto

El artículo estudia el sistema como un juego de aprendizaje. Los agentes observan desempeño, comparan resultados, infieren la contribución relativa del vehículo y del piloto, y ajustan su disposición a pagar. En ese contexto, la subasta no es solo un mecanismo de asignación, sino también un mecanismo de agregación de información. Cuanto más refinadas sean las valoraciones privadas, más informativa será la señal pública de mercado y más alineada estará la eficiencia monetaria con el valor económico revelado por el propio sistema.

### Esqueleto sugerido

1. Introducción
2. Estructura informacional del entorno
3. Formación de creencias y error de valoración
4. Aprendizaje del mercado y ajuste intertemporal
5. Pujas honestas y calidad de señal
6. Precio descubierto y precisión de la eficiencia medida
7. Discusión sobre mercados delgados y aprendizaje imperfecto
8. Conclusiones

### Núcleo formal

- error de valoración privada;
- aprendizaje o actualización de creencias;
- relación entre sinceridad de puja y precio revelado;
- relación entre calidad de señal del precio y precisión del residual de eficiencia.

### Proposiciones naturales

- mejores valoraciones privadas producen señales de mercado más fieles;
- señales más fieles producen mediciones de eficiencia más informativas;
- el sistema incentiva inversión cognitiva en conocimiento técnico y deportivo.

### Lo que debe quedar fuera

- programa reglamentario completo;
- pruebas empíricas extensas;
- defensa global del ecosistema como institución.

---

## Artículo 4. Libertad tecnológica, disciplina del gasto y diseño institucional

### Pregunta central

¿Qué implicaciones institucionales tiene un sistema que premia eficiencia relativa al mercado en lugar de gasto absoluto o velocidad bruta?

### Tesis

Un sistema que premia desempeño relativo al valor descubierto por mercado permite mayor libertad tecnológica sin perder disciplina competitiva, porque desplaza la presión desde la restricción ex ante del diseño hacia la evaluación ex post de la eficiencia monetaria.

### Resumen abstracto

El artículo estudia las consecuencias institucionales del mecanismo anterior para el diseño de reglas técnicas. Si el premio principal se asigna según eficiencia monetaria y no según gasto bruto, el sistema no necesita depender exclusivamente de reglamentos muy restrictivos para controlar la escalada de costos. En cambio, puede tolerar mayor libertad de diseño, ya que el exceso de gasto no asegura el mejor resultado económico. La competencia se desplaza así desde la mera capacidad de gastar hacia la capacidad de convertir recursos en desempeño valioso.

### Esqueleto sugerido

1. Introducción
2. Gasto bruto, desempeño y disciplina competitiva
3. Libertad tecnológica bajo premio por eficiencia
4. Precio descubierto como corrector institucional del sobregasto
5. Implicaciones para reglamento técnico
6. Riesgos institucionales y límites del argumento
7. Conclusiones

### Núcleo formal

- relación entre gasto, precio descubierto y premio esperado;
- comparación conceptual entre disciplina por prohibición y disciplina por incentivos;
- condiciones bajo las cuales el sistema desalienta gasto ineficiente.

### Lo que debe quedar fuera

- detalles finos de implementación de la app;
- todas las excepciones administrativas posibles;
- modelación econométrica detallada.

---

## Artículo 5. Simulaciones, ejemplos numéricos y exploración computacional

### Pregunta central

¿Qué dinámicas observables genera el sistema bajo distintos parámetros de valoración, inversión, pilotaje y profundidad de mercado?

### Tesis

Las simulaciones muestran que el mecanismo produce patrones consistentes con la teoría: el premio favorece rendimiento eficiente, la señal de mercado mejora cuando las valoraciones son más informadas y las decisiones de inversión tienden a reorganizarse en torno al desempeño relativo al costo.

### Resumen abstracto

El artículo toma las estructuras teóricas de los trabajos anteriores y las traduce a escenarios numéricos y simulados. Se comparan agentes homogéneos y heterogéneos, distintos grados de habilidad de pilotaje, distintas tecnologías y diferentes niveles de profundidad de mercado. El objetivo no es demostrar una ley empírica universal, sino explorar la robustez cualitativa de las proposiciones teóricas y localizar las condiciones bajo las cuales el sistema funciona mejor o peor.

### Esqueleto sugerido

1. Introducción
2. Parámetros y entorno de simulación
3. Escenario base homogéneo
4. Heterogeneidad tecnológica y de pilotaje
5. Profundidad de mercado y calidad de señal
6. Sensibilidad del premio y del precio descubierto
7. Lectura económica de los resultados
8. Conclusiones

### Núcleo formal

- definición de escenarios;
- comparación entre trayectorias de puja, precio, eficiencia y premio;
- análisis cualitativo de robustez.

### Lo que debe quedar fuera

- nuevas teorías institucionales de fondo;
- discusiones reglamentarias generales que no dependan de la simulación.

---

## Orden recomendado de escritura

1. Artículo 1, porque fija el mecanismo base.
2. Artículo 2, porque da la teoría de equilibrio que parece ser tu tesis más fuerte.
3. Artículo 3, porque explica por qué el sistema incentiva estudiar tecnología, pilotaje y mercado.
4. Artículo 4, porque extrae consecuencias institucionales una vez entendido el mecanismo.
5. Artículo 5, porque sirve para validar, ilustrar y explorar límites.

## Observación final

El error más probable sería intentar fusionar los cinco en un solo paper. Eso debilitaría el foco y mezclaría capas analíticas distintas. La fuerza del programa está precisamente en que cada artículo resuelve una pregunta clara y deja preparada la siguiente.