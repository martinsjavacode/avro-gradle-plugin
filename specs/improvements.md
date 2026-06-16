# Spec: Melhorias do Avro Gradle Plugin

**Data:** 2026-06-16  
**Versão atual:** 1.2.0  
**Status:** Proposta

---

## 1. Remover uso de `afterEvaluate` — Lazy Configuration

### Problema

O `AvroGradlePlugin` usa `project.afterEvaluate` para configurar tasks, o que é considerado anti-pattern em plugins Gradle modernos. Isso causa problemas de ordenação quando múltiplos plugins interagem e impede a configuração lazy de tasks.

### Solução

Usar `Property`/`Provider` de ponta a ponta, conectando as propriedades da extensão diretamente às tasks via convention mapping ou `convention()`.

### Mudanças

- `AvroPluginExtension` passa a usar `Property<String>` e `Property<Boolean>` em vez de tipos primitivos
- Tasks recebem providers da extensão diretamente no registro (sem `afterEvaluate`)
- `SourceDirResolver` retorna `Provider<Directory>` em vez de `File`

### Exemplo

```kotlin
abstract class AvroPluginExtension {
    abstract val sourceDir: Property<String>
    abstract val outputDir: Property<String>
    abstract val fieldVisibility: Property<String>
    // ...

    init {
        sourceDir.convention("src/main/resources/avro")
        outputDir.convention("generated/java")
        fieldVisibility.convention("PUBLIC")
    }
}
```

```kotlin
// No plugin apply, sem afterEvaluate
project.tasks.register("generateAvroClasses", AvroTask::class.java) {
    sourceDir.set(project.layout.projectDirectory.dir(extension.sourceDir))
    outputDir.set(project.layout.buildDirectory.dir(extension.outputDir))
    fieldVisibility.set(extension.fieldVisibility)
    // ...
}
```

### Impacto

- Breaking change na DSL (usuários que usam `=` precisam usar `.set()` no Kotlin DSL)
- Melhor performance (tasks não configuradas não são materializadas)
- Compatibilidade com configuration cache do Gradle

---

## 2. Eliminar recriação de `AvroPluginExtension` na Task

### Problema

`AvroTask.generateAvroClasses()` cria um novo `AvroPluginExtension()` e copia manualmente os valores das properties da task para ele, apenas para passar ao `AvroGenerator`. Isso é duplicação frágil — se uma property nova for adicionada na extensão, é necessário lembrar de mapeá-la aqui também.

### Solução

Refatorar `AvroGenerator.process()` para receber os parâmetros individuais (ou um data class dedicado) em vez da extensão.

### Mudanças

```kotlin
data class AvroGeneratorConfig(
    val fieldVisibility: String,
    val stringType: String,
    val optionalGetters: Boolean,
    val useDecimalLogical: Boolean,
    val createNullSafeAnnotations: Boolean,
)
```

```kotlin
// AvroGenerator.process() passa a receber AvroGeneratorConfig
fun process(
    sourceDir: File,
    outputDirectory: File,
    config: AvroGeneratorConfig,
    reportDir: File,
    logger: Logger,
): GenerationReport
```

```kotlin
// Na task, constrói o config diretamente das properties
val config = AvroGeneratorConfig(
    fieldVisibility = fieldVisibility.get(),
    stringType = stringType.get(),
    optionalGetters = optionalGetters.get(),
    useDecimalLogical = useDecimalLogical.get(),
    createNullSafeAnnotations = createNullSafeAnnotations.get(),
)
```

### Impacto

- Elimina dependência do generator na classe de extensão
- Facilita testes unitários do generator
- Sem breaking change para usuários

---

## 3. Melhorar `SourceDirResolver` — Parsing YAML robusto

### Problema

O `parseYamlProperty` atual faz parsing linha-a-linha e só suporta propriedades top-level. Qualquer YAML com nesting, comentários inline, ou valores entre aspas pode falhar silenciosamente.

### Solução

Duas opções:

**Opção A (mínima):** Remover o suporte a YAML/properties e confiar apenas na DSL do plugin. Simplifica o código e evita comportamento surpresa.

**Opção B (robusta):** Usar SnakeYAML (já transitiva do Gradle) para parsing correto.

### Recomendação

Opção A. A configuração via `application.properties`/`application.yml` é um acoplamento desnecessário com Spring Boot. O plugin Gradle deve ser configurado via DSL do Gradle.

### Mudanças (Opção A)

- Remover `SourceDirResolver`
- Usar apenas `extension.sourceDir` com convention default
- Documentar no README que o diretório deve ser configurado no bloco `avro {}`

### Mudanças (Opção B)

```kotlin
private fun parseYamlProperty(file: File, key: String): String? {
    val yaml = org.yaml.snakeyaml.Yaml()
    val data = file.inputStream().use { yaml.load<Map<String, Any>>(it) }
    return data?.get(key)?.toString()
}
```

### Impacto

- Opção A: breaking change para quem usa `application.properties` para configurar o plugin (provavelmente ninguém)
- Opção B: sem breaking change, parsing mais confiável

---

## 4. Suporte a Avro IDL (`.avdl`)

### Problema

O plugin só suporta `.avsc` (JSON schema) e `.avpr` (protocol JSON). O formato `.avdl` (Avro IDL) é amplamente usado por ser mais legível e suportar imports entre schemas.

### Solução

Adicionar processamento de arquivos `.avdl` usando `org.apache.avro.compiler.idl.Idl`.

### Mudanças

- Adicionar `"avdl"` na lista de extensões suportadas em `AvroGenerator` e `SchemaValidator`
- Implementar `processAvdl()`:

```kotlin
private fun processAvdl(
    file: File,
    extension: AvroPluginExtension,
    outputDirectory: File,
    logger: Logger,
    report: GenerationReport,
) {
    val idl = Idl(file)
    val protocol = idl.CompilationUnit()
    protocol.types.forEach { schema ->
        validateSchema(schema)
        val outputFile = compileSchema(schema, file, extension, outputDirectory)
        report.addClass(
            GeneratedClass(
                name = schema.name,
                sourceFile = file.name,
                outputFile = outputFile.relativeTo(outputDirectory).path,
                type = "AVDL",
            ),
        )
        logger.lifecycle("Generated class from AVDL: ${schema.name}")
    }
    idl.close()
}
```

- Atualizar `schemaFiles` nas tasks: `include("**/*.avsc", "**/*.avpr", "**/*.avdl")`
- Atualizar README e tabela de features

### Impacto

- Sem breaking change (apenas adição)
- Aumenta o público-alvo do plugin significativamente

---

## 5. Substituir template HTML por engine leve

### Problema

O relatório HTML é gerado com string template Kotlin (~60 linhas de HTML inline). Isso é difícil de manter, não tem syntax highlighting no IDE, e mistura lógica com apresentação.

### Solução

Extrair o template para um resource file e usar substituição simples, ou usar kotlinx.html para geração type-safe.

### Recomendação

Usar um template como resource (`/templates/report.html`) com placeholders (`{{classes}}`, `{{duration}}`, etc.) para manter o plugin leve sem dependências extras.

### Mudanças

- Criar `src/main/resources/templates/avro-generation-report.html` com placeholders
- `GenerationReport.generateHtmlReport()` lê o template e faz substituições:

```kotlin
fun generateHtmlReport(outputDir: File) {
    val duration = System.currentTimeMillis() - startTime
    val template = javaClass.getResource("/templates/avro-generation-report.html")!!.readText()

    val rowsHtml = classes.joinToString("\n") {
        "<tr><td>${it.name}</td><td>${it.type}</td><td>${it.sourceFile}</td><td>${it.outputFile}</td></tr>"
    }

    val html = template
        .replace("{{timestamp}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .replace("{{totalClasses}}", classes.size.toString())
        .replace("{{duration}}", duration.toString())
        .replace("{{rows}}", rowsHtml)

    File(outputDir, "avro-generation-report.html").writeText(html)
}
```

### Impacto

- Sem breaking change
- Template editável sem recompilar o plugin
- Separação clara entre lógica e apresentação

---

## Priorização

| # | Melhoria | Esforço | Valor | Breaking Change |
|---|---|---|---|---|
| 1 | Lazy Configuration (remover afterEvaluate) | Alto | Alto | Sim |
| 2 | Eliminar recriação da Extension | Baixo | Médio | Não |
| 4 | Suporte a `.avdl` | Médio | Alto | Não |
| 3 | Remover/melhorar SourceDirResolver | Baixo | Baixo | Depende |
| 5 | Template HTML como resource | Baixo | Baixo | Não |

### Ordem de execução sugerida

1. **#2** — refactoring interno simples, prepara terreno
2. **#4** — feature mais pedida, alto valor
3. **#1** — refactoring estrutural, pode ser feito junto com bump de major version
4. **#5** — melhoria de manutenibilidade
5. **#3** — simplificação (Opção A) ou melhoria pontual (Opção B)
