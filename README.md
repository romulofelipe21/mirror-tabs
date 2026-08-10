# Mirror Tabs

Plugin externo para RuneLite que mostra inventário e equipamento ao mesmo tempo, mantendo o painel original ativo e exibindo o outro em um overlay espelhado.

Quando a aba de inventário está selecionada, o overlay mostra os itens equipados. Quando a aba de equipamento está selecionada, o overlay mostra o inventário. O último espelho permanece visível mesmo se o painel lateral do jogo for recolhido ou o painel de configurações do RuneLite for aberto.

O espelho acompanha automaticamente a aba ativa. A janela pode ser movida e redimensionada livremente com o gerenciador de overlays do RuneLite. Os slots e itens espelhados permanecem somente para leitura: não equipam, usam, movem ou descartam itens e não enviam ações ao servidor.

## Requisitos

- Windows, macOS ou Linux
- JDK 11 instalado e disponível no `PATH` (um JRE isolado não contém o compilador `javac`)
- Conexão com a internet na primeira compilação, para o Gradle baixar o RuneLite e as demais dependências

Baixe um JDK 11, se necessário, no site do [Eclipse Adoptium](https://adoptium.net/temurin/releases/?version=11). Depois, abra um novo terminal e confirme que `java` e `javac` estão disponíveis:

```powershell
java -version
javac -version
```

Se houver mais de uma instalação do Java, selecione o JDK 11 na sessão atual antes de compilar:

```powershell
$env:JAVA_HOME = "C:\caminho\para\o\jdk-11"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Compilar no Windows

Abra o PowerShell e execute:

```powershell
git clone https://github.com/romulofelipe21/mirror-tabs.git
cd mirror-tabs
.\gradlew.bat clean build
```

O artefato compilado será criado em `build\libs`.

## Rodar localmente

Na raiz do projeto, execute:

```powershell
.\gradlew.bat run
```

O cliente de desenvolvimento do RuneLite será aberto com o plugin carregado. Nas configurações do RuneLite, procure por `Mirror Tabs` e mantenha o plugin ativado.

Para testar:

1. Abra a aba de inventário e confirme que o equipamento aparece no overlay.
2. Abra a aba de equipamento e confirme que o inventário aparece no overlay.
3. Equipe, desequipe, mova ou empilhe itens e confirme que o espelho é atualizado.
4. Segure `Alt`, arraste o interior da janela e confirme que ela pode ser posicionada livremente.
5. Ainda segurando `Alt`, arraste uma borda ou um canto da janela e confirme que o conteúdo se adapta ao novo tamanho.
6. Confirme que os slots e itens do overlay não respondem a cliques.

O RuneLite salva a posição e o tamanho escolhidos. Para restaurar o posicionamento padrão, segure `Alt` e clique com o botão direito sobre o overlay. Se você alterou o atalho de movimentação de overlays nas configurações do RuneLite, use o atalho configurado no lugar de `Alt`.

Para entrar com uma Jagex Account no cliente de desenvolvimento, siga o guia oficial: [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## Opcoes atuais

- `enableMirrorTabs`: mostra ou esconde o overlay.
- `overlayOpacity`: ajusta a opacidade entre 0% e 100%.
- `overlayScale`: define a escala inicial entre 50% e 200%; depois, o tamanho escolhido ao arrastar as bordas fica salvo pelo RuneLite.
- `showLabels`: mostra ou esconde o texto do overlay.

## Estrutura principal

- `MirrorTabsPlugin`: ciclo de vida do plugin e registro do overlay.
- `MirrorTabsConfig`: configurações básicas.
- `MirrorTabsOverlay`: detecta a aba ativa e desenha o contêiner oposto em modo somente leitura.
- `MirrorTabState`: relaciona os estados `INVENTORY` e `EQUIPMENT` aos contêineres do RuneLite.

## Escopo desta versao

Esta versão não implementa uso, equipagem ou movimentação de itens pelo overlay. Ela não trata cliques nos slots nem envia ações ao jogo; apenas o gerenciador nativo de overlays do RuneLite movimenta e redimensiona a janela.
