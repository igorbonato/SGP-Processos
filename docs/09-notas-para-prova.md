# 09 — Notas para Prova (cheat sheet do concurso)

> Diferente dos docs `01`-`08` (que explicam a arquitetura e o "porquê" das decisões de design),
> este arquivo é uma **folha de revisão rápida**, no formato pergunta/resposta, organizada pelas
> mesmas seções do edital (`conteudo_programatico.md`, raiz do repo). Ele é **cumulativo**: cresce
> a cada fase implementada, sempre que uma nuance com valor de prova aparecer no código — ver
> a convenção em [`08-roadmap-implementacao.md`](08-roadmap-implementacao.md).

---

## 2. Frameworks Java

### Jakarta EE 8 usa o namespace `javax.*` ou `jakarta.*`?

**`javax.*`.** O rename de pacote para `jakarta.*` só aconteceu a partir do **Jakarta EE 9** (fim de
2020). A Eclipse Foundation assumiu a especificação da Oracle em 2017/2018 e rebatizou o projeto de
"Java EE" para "Jakarta EE", mas a Oracle não cedeu os direitos sobre o prefixo de pacote `javax` —
então, até a versão 8, o nome do projeto já era "Jakarta EE" mas o código continuava `javax.*`.

> **Pegadinha:** `javax.persistence.Entity` (Jakarta EE 8) e `jakarta.persistence.Entity`
> (Jakarta EE 9+) são a **mesma anotação**, em versões diferentes da especificação — não são coisas
> diferentes, e não há paralelo com nada do .NET (a Microsoft nunca precisou renomear um namespace
> inteiro por disputa de marca).
>
> _Fonte: [`05-especificacao-vs-implementacao.md`](05-especificacao-vs-implementacao.md), corrigido na Fase 0._

### Zuul funciona com Spring Boot 3?

**Não.** O suporte ao Netflix Zuul foi removido do Spring Cloud a partir do release train
`2020.0 (Ilford)`, que já exige Spring Boot 2.4+ rumo ao 3.x (namespace `jakarta.*`). O último trem
que ainda inclui Zuul é o **Hoxton**, compatível com **Spring Boot 2.3.x/2.4.x**.

> **Pegadinha clássica de entrevista/prova:** perguntar "qual API Gateway usar com Spring Boot 3" e
> responder "Zuul" está errado — o sucessor recomendado pelo próprio Spring é o **Spring Cloud
> Gateway**. Nós usamos Zuul propositalmente porque o **edital pede especificamente Zuul**, o que só
> é tecnicamente viável fixando a geração Boot 2.3.x / Spring Cloud Hoxton.SR12 — que, como bônus,
> já fala `javax.*` (Jakarta EE 8), fechando com o resto do edital.
>
> _Fonte: `pom.xml` raiz (comentário na tag `<parent>`), decisão da Fase 0._

---

## Como usar este arquivo

- Cada fase futura do [roadmap](08-roadmap-implementacao.md) pode adicionar novas seções/entradas
  aqui, sempre no formato: pergunta objetiva → resposta direta → bloco de citação com a pegadinha
  ou o motivo de confundir quem vem do .NET → fonte (doc ou fase onde foi decidido/explicado).
- As seções seguem os **títulos exatos** do `conteudo_programatico.md` para facilitar o cruzamento
  na hora de revisar para a prova.
