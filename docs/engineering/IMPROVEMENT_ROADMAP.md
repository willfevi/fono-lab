# Roadmap de engenharia

| Ordem | Entrega | Impacto | Esforço | Risco |
| --- | --- | --- | --- | --- |
| 1 | Separar os tipos de data e arquivo que colidem com JVM e isolar o comportamento Wasm | Alto | Médio | Alto |
| 2 | Validar migração Room v1 → v2 e integrar persistência real ao fluxo Android | Alto | Alto | Alto |
| 3 | Definir armazenamento local e recuperação para Windows e Web, incluindo exportação segura | Alto | Alto | Alto |
| 4 | Estabilizar empacotamento, lint e testes por plataforma. Incluir os smoke tests e a verificação de dependências e segredos | Alto | Médio | Médio |
| 5 | Medir startup, listas, formulários, exportação e jogos. Corrigir o gargalo confirmado e verificar teclado e leitor de tela nos três alvos | Médio | Médio | Baixo a médio |

## Plano sujeito a aprovação

1. **Compatibilidade JVM/Wasm.** Catalogar tipos, chamadas e formatos em `java.time`, `java.io` e `java.util`. Introduzir limites de plataforma em pequenas alterações. Manter JDK no JVM e preservar a semântica e os formatos existentes no Web. Verificar fixtures com fuso, idade, datas limítrofes e arquivos. Aceite com testes Desktop verdes e execução básica nos três alvos. Reverter o diff do adaptador que causar divergência.
2. **Dados locais Android.** Fazer backup verificável de bases sintéticas v1/v2, implementar abertura preservadora, testar rollback e transações de anamnese. Ligar Room a um único fluxo de UI em etapa separada e comparar cadastros, relações, histórico e resultados com a base de teste. Reverter a seleção de contêiner e restaurar uma cópia validada em caso de falha. Qualquer decisão sobre dados em memória de uma sessão ativa precisa de estratégia explícita.
3. **Persistência Windows e Web.** Comparar opções suportadas por cada alvo sob os mesmos contratos, com reinício, quota do navegador, concorrência, exportação, restauração e isolamento. Usar fixtures sintéticas e introduzir armazenamento por alvo. Rollback deve preservar a leitura ou a recuperação dos dados já gravados.

A migração para as APIs do AGP 9.4.1 foi feita em uma etapa própria. Atualizações de versões e reestruturações de módulos seguem como etapas separadas. Avaliar Detekt para análise semântica, ktlint ou Spotless para formatação incremental e Gitleaks/OSV-Scanner para segurança. Adoção depende de compatibilidade, custo e execução em CI. O wrapper, o lint Android e as suítes existentes formam a primeira etapa de validação.
