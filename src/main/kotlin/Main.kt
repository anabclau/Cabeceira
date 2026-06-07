
import kotlin.math.ceil


// =============================================================
// INTERFACES
// =============================================================

//apresenta os detalhes de determinado objeto
interface Detalhes {
    // Toda obra precisa saber se descrever como texto.
    fun exibirDetalhes(): String
}

//verifica se uma obra é concluida e apresenta seu percentual caso não seja
interface Concluivel {
    fun concluido(): Boolean
    fun percentualConcluido(): Double
}

// representa  uma meta diária de progresso. Nem toda obra precisa ter meta

interface Desafiavel {
    val metaDiaria: Int                            // unidades por dia (páginas ou minutos)
    fun metaCumprida(progressoHoje: Int): Boolean  // o leitor bateu a meta?
    fun diasRestantes(): Int                       // quantos dias faltam no ritmo da meta
}


// =============================================================
// SEALED CLASS — resultado tipado de atualizarProgresso
// =============================================================

// Em vez de retornar uma String solta como "Sucesso" ou "Erro: ...",
// uso uma sealed class. conjunto fechado de possibilidades.
sealed class ResultadoProgresso {
    // Sucesso não carrega dados extras — um 'object' é suficiente.
    object Sucesso : ResultadoProgresso()

    // Erro carrega a mensagem do problema — uso 'data class'.
    data class Erro(val mensagem: String) : ResultadoProgresso()
}


abstract class Obra(
    val titulo: String,
    val autor: String
) : Detalhes, Concluivel {

    // ID com leitura pública mas escrita privada.
    // Ninguém de fora pode mudar o ID diretamente — só via atribuirId().
    var id: Int = -1
        private set

    // Uso 'internal' para que somente o SistemaLeituras (mesmo módulo)
    // possa atribuir IDs. É mais seguro que deixar o método público.
    internal fun atribuirId(novoId: Int) {
        id = novoId
    }

    // Cada subclasse implementa o avanço de progresso do seu jeito.
    abstract fun atualizarProgresso(valor: Int): ResultadoProgresso

    // Cada subclasse sabe dizer quanto falta, na sua unidade.
    abstract fun tempoRestante(): String
}


// =============================================================
// SUBCLASSE: Livro
// =============================================================

// Livro herda de Obra e implementa adicionalmente Desafiavel.
// O polimorfismo aparece aqui: atualizarProgresso(), concluido() e
// exibirDetalhes() têm comportamentos completamente diferentes
// em Livro, AudioBook e Manga, mas são chamados da mesma forma.
class Livro(
    titulo: String,
    autor: String,
    private val paginasTotal: Int,        // encapsulado: só Livro acessa diretamente
    override val metaDiaria: Int = 20     // implementa Desafiavel — padrão: 20 pág/dia
) : Obra(titulo, autor), Desafiavel {

    // Estado interno — só muda via atualizarProgresso().
    private var paginaAtual: Int = 0

    // Polimorfismo: Livro resolve progresso em páginas.
    override fun atualizarProgresso(valor: Int): ResultadoProgresso {
        if (valor <= 0)
            return ResultadoProgresso.Erro("O número de páginas deve ser maior que zero.")
        if (paginaAtual + valor > paginasTotal)
            return ResultadoProgresso.Erro(
                "Esse progresso ultrapassa o total de $paginasTotal páginas."
            )
        paginaAtual += valor
        return ResultadoProgresso.Sucesso
    }

    override fun tempoRestante(): String = "${paginasTotal - paginaAtual} páginas restantes"

    // Implementação de Concluivel.
    override fun concluido(): Boolean = paginaAtual == paginasTotal
    override fun percentualConcluido(): Double =
        if (paginasTotal == 0) 0.0
        else (paginaAtual.toDouble() / paginasTotal) * 100

    // Implementação de Desafiavel.
    override fun metaCumprida(progressoHoje: Int): Boolean = progressoHoje >= metaDiaria
    override fun diasRestantes(): Int {
        val restante = paginasTotal - paginaAtual
        return if (metaDiaria == 0) -1 else ceil(restante.toDouble() / metaDiaria).toInt()
    }

    // Polimorfismo: a versão de Livro inclui páginas e previsão de dias.
    override fun exibirDetalhes(): String {
        val barra = barraProgresso(percentualConcluido())
        return """
            |📖 LIVRO [ID: $id]
            |Título    : $titulo
            |Autor     : $autor
            |Progresso : $paginaAtual/$paginasTotal páginas
            |$barra ${"%.1f".format(percentualConcluido())}%
            |Meta/dia  : $metaDiaria págs → ~${diasRestantes()} dias restantes
            |${if (concluido()) "✅ Concluído!" else tempoRestante()}
        """.trimMargin()
    }
}


// =============================================================
// SUBCLASSE: AudioBook
// =============================================================

// AudioBook herda de Obra e Desafiavel, mas trabalha com minutos.
// A estrutura é idêntica à de Livro — o polimorfismo garante
// que o sistema os trata pelo mesmo contrato (Obra), sem if/else.
class AudioBook(
    titulo: String,
    autor: String,
    private val duracaoTotal: Int,        // em minutos
    override val metaDiaria: Int = 30     // padrão: 30 min/dia
) : Obra(titulo, autor), Desafiavel {

    private var duracaoAtual: Int = 0

    // Polimorfismo: AudioBook resolve progresso em minutos.
    override fun atualizarProgresso(valor: Int): ResultadoProgresso {
        if (valor <= 0)
            return ResultadoProgresso.Erro("O tempo escutado deve ser maior que zero.")
        if (duracaoAtual + valor > duracaoTotal)
            return ResultadoProgresso.Erro(
                "Esse progresso ultrapassa a duração total de $duracaoTotal minutos."
            )
        duracaoAtual += valor
        return ResultadoProgresso.Sucesso
    }

    override fun tempoRestante(): String = "${duracaoTotal - duracaoAtual} minutos restantes"
    override fun concluido(): Boolean = duracaoAtual >= duracaoTotal
    override fun percentualConcluido(): Double =
        if (duracaoTotal == 0) 0.0
        else (duracaoAtual.toDouble() / duracaoTotal) * 100

    override fun metaCumprida(progressoHoje: Int): Boolean = progressoHoje >= metaDiaria
    override fun diasRestantes(): Int {
        val restante = duracaoTotal - duracaoAtual
        return if (metaDiaria == 0) -1 else ceil(restante.toDouble() / metaDiaria).toInt()
    }

    override fun exibirDetalhes(): String {
        val barra = barraProgresso(percentualConcluido())
        return """
            |🎧 AUDIOBOOK [ID: $id]
            |Título    : $titulo
            |Autor     : $autor
            |Progresso : $duracaoAtual/$duracaoTotal minutos
            |$barra ${"%.1f".format(percentualConcluido())}%
            |Meta/dia  : $metaDiaria min → ~${diasRestantes()} dias restantes
            |${if (concluido()) "✅ Concluído!" else tempoRestante()}
        """.trimMargin()
    }
}


// =============================================================
// SUBCLASSE: Manga
// =============================================================

// Manga é a terceira subclasse — adicionada sem alterar nenhuma
// linha da classe Obra ou do SistemaLeituras. Isso demonstra
// na prática o benefício da herança: o sistema é aberto para
// extensão sem precisar de modificação.
// Manga tem dois níveis: capítulo atual e volume calculado.
class Manga(
    titulo: String,
    autor: String,
    private val capitulosTotal: Int,
    private val capitulosPorVolume: Int = 10  // quantos capítulos formam um volume
) : Obra(titulo, autor) {

    private var capituloAtual: Int = 0

    // Propriedades calculadas — o 'get()' recalcula sempre que acessadas.
    val volumeAtual: Int  get() = (capituloAtual / capitulosPorVolume) + 1
    val volumesTotal: Int get() = ceil(capitulosTotal.toDouble() / capitulosPorVolume).toInt()

    override fun atualizarProgresso(valor: Int): ResultadoProgresso {
        if (valor <= 0)
            return ResultadoProgresso.Erro("O número de capítulos deve ser maior que zero.")
        if (capituloAtual + valor > capitulosTotal)
            return ResultadoProgresso.Erro(
                "Esse progresso ultrapassa o total de $capitulosTotal capítulos."
            )
        capituloAtual += valor
        return ResultadoProgresso.Sucesso
    }

    override fun tempoRestante(): String =
        "${capitulosTotal - capituloAtual} capítulos restantes " +
                "(${volumesTotal - volumeAtual + 1} volumes)"

    override fun concluido(): Boolean = capituloAtual == capitulosTotal
    override fun percentualConcluido(): Double =
        if (capitulosTotal == 0) 0.0
        else (capituloAtual.toDouble() / capitulosTotal) * 100

    override fun exibirDetalhes(): String {
        val barra = barraProgresso(percentualConcluido())
        return """
            |📚 MANGA [ID: $id]
            |Título    : $titulo
            |Autor     : $autor
            |Capítulo  : $capituloAtual/$capitulosTotal
            |Volume    : $volumeAtual/$volumesTotal
            |$barra ${"%.1f".format(percentualConcluido())}%
            |${if (concluido()) "✅ Concluído!" else tempoRestante()}
        """.trimMargin()
    }
}


// =============================================================
// FUNÇÃO UTILITÁRIA — barra de progresso visual no terminal
// =============================================================

// Fica fora das classes porque é uma utilidade genérica,
// não pertence a nenhum tipo específico de obra.
fun barraProgresso(percentual: Double, tamanho: Int = 20): String {
    val preenchido = ((percentual / 100) * tamanho).toInt()
    val vazio = tamanho - preenchido
    return "[" + "█".repeat(preenchido) + "░".repeat(vazio) + "]"
}


// =============================================================
// DATA CLASS — RegistroLeitura
// =============================================================

// 'data class' é ideal para contêineres de dados imutáveis.
// O Kotlin gera equals(), hashCode() e toString() automaticamente.
// Cada sessão de leitura gera um registro — incluindo avaliações.
data class RegistroLeitura(
    val obraId: Int,
    val tipoObra: String,
    val progressoRealizado: Int,
    val notaFinal: Int? = null,       // opcional — só na avaliação final
    val comentario: String? = null    // opcional — opinião do leitor
) {
    override fun toString(): String =
        """
        |📌 Registro
        |Obra ID    : $obraId ($tipoObra)
        |Progresso  : +$progressoRealizado
        |Nota final : ${notaFinal?.let { "★".repeat(it) + " ($it/5)" } ?: "—"}
        |Comentário : ${comentario ?: "—"}
        """.trimMargin()
}


// =============================================================
// SISTEMA CENTRAL — SistemaLeituras (Encapsulamento)
// =============================================================

// É o coração do app. Gerencia obras e registros.
// As listas internas são privadas — nenhum código externo
// pode adicionar, remover ou alterar obras diretamente.
// Tudo passa pelos métodos públicos controlados.
class SistemaLeituras {

    private val obras     = mutableListOf<Obra>()
    private val registros = mutableListOf<RegistroLeitura>()

    // Contador Único de ID — Todas as obras compartilham a mesma sequência.
    private var proximoId = 0

    // Adiciono uma obra e atribuo o ID global.
    fun adicionarObra(obra: Obra) {
        obra.atribuirId(proximoId++)
        obras.add(obra)
        println("✅ \"${obra.titulo}\" cadastrada com sucesso! [ID: ${obra.id}]")
    }

    // Listo obras chamando exibirDetalhes() sem saber o tipo concreto —
    // polimorfismo puro. Posso ordenar por progresso opcionalmente.
    fun listarObras(ordenarPorProgresso: Boolean = false) {
        if (obras.isEmpty()) { println("Nenhuma obra cadastrada ainda."); return }
        val lista = if (ordenarPorProgresso)
            obras.sortedByDescending { it.percentualConcluido() }
        else
            obras.toList()
        lista.forEach { println("\n${it.exibirDetalhes()}\n" + "─".repeat(45)) }
    }

    // Busco apenas pelo ID, pois agora ele é único para todas as obras.
    fun buscarObra(id: Int): Obra? = obras.find { it.id == id }

    // Registro uma sessão de leitura e aviso sobre a meta do dia.
    fun registrarLeitura(idObra: Int, progresso: Int): String {
        val obra = buscarObra(idObra) ?: return "❌ Obra com ID $idObra não encontrada."

        return when (val resultado = obra.atualizarProgresso(progresso)) {
            is ResultadoProgresso.Sucesso -> {
                registros.add(
                    RegistroLeitura(
                        obraId             = idObra,
                        tipoObra           = nomeDoTipo(obra),
                        progressoRealizado = progresso
                    )
                )
                // Verifico a meta do dia aproveitando polimorfismo com Desafiavel.
                // 'is Desafiavel' funciona porque Kotlin faz smart cast automaticamente.
                val avisoMeta = if (obra is Desafiavel) {
                    if (obra.metaCumprida(progresso)) " 🏆 Meta do dia batida!"
                    else " (meta: ${obra.metaDiaria} — faltam ${obra.metaDiaria - progresso})"
                } else ""

                if (obra.concluido()) "🎉 Obra concluída! Não esqueça de avaliar."
                else "✅ ${obra.tempoRestante()}$avisoMeta"
            }
            is ResultadoProgresso.Erro -> "❌ ${resultado.mensagem}"
        }
    }

    // Avaliação final — só aceito se a obra estiver concluída.
    fun avaliarObra(idObra: Int, nota: Int, comentario: String? = null): String {
        if (nota !in 1..5) return "❌ Nota deve ser entre 1 e 5."
        val obra = buscarObra(idObra) ?: return "❌ Obra com ID $idObra não encontrada."
        if (!obra.concluido()) return "❌ Só posso avaliar obras concluídas."

        registros.add(
            RegistroLeitura(
                obraId             = idObra,
                tipoObra           = nomeDoTipo(obra),
                progressoRealizado = 0,
                notaFinal          = nota,
                comentario         = comentario
            )
        )
        return "⭐ Avaliação registrada para \"${obra.titulo}\"!"
    }

    fun listarRegistros() {
        if (registros.isEmpty()) { println("Nenhum registro ainda."); return }
        registros.forEach { println("\n$it\n" + "─".repeat(35)) }
    }



    // Resumo geral — uso polimorfismo via interfaces aqui:
    // obras.count { it.concluido() } funciona para qualquer subclasse
    // sem precisar saber se é Livro, AudioBook ou Manga.
    fun resumo() {
        val total      = obras.size
        val concluidas = obras.count { it.concluido() }
        val notas      = registros.mapNotNull { it.notaFinal }
        val media      = if (notas.isEmpty()) "—" else "${"%.1f".format(notas.average())} ⭐"

        println("""
            |===== RESUMO =====
            |Obras cadastradas : $total
            |  📖 Livros       : ${obras.count { it is Livro }}
            |  🎧 Audiobooks   : ${obras.count { it is AudioBook }}
            |  📚 Mangas       : ${obras.count { it is Manga }}
            |Concluídas        : $concluidas
            |Em andamento      : ${total - concluidas}
            |Nota média        : $media
            |Comentários       : ${registros.count { it.comentario != null }}
        """.trimMargin())
    }

    // Helper privado — retorna o nome legível do tipo da obra.
    private fun nomeDoTipo(obra: Obra): String = when (obra) {
        is Livro     -> "Livro"
        is AudioBook -> "AudioBook"
        is Manga     -> "Manga"
        else         -> "Desconhecido"
    }
}


// =============================================================
// MENU PRINCIPAL
// =============================================================

fun main() {
    val sistema = SistemaLeituras()
    var opcao: Int

    do {
        println("""
            |
            |╔══════════════════════════╗
            |║      📚  CABECEIRA       ║
            |╚══════════════════════════╝
            |─── Cadastro ───────────────
            | 1  Cadastrar Livro
            | 2  Cadastrar Audiobook
            | 3  Cadastrar Manga
            |─── Leituras ───────────────
            | 4  Registrar Leitura
            | 5  Avaliar Obra Concluída
            |─── Consultas ──────────────
            | 6  Listar Obras
            | 7  Listar Obras por Progresso
            | 8  Listar Registros
            | 9  Ver Resumo
            | 0  Sair
        """.trimMargin())

        // toIntOrNull() evita crash se o usuário digitar texto.
        opcao = readln().toIntOrNull() ?: -1

        when (opcao) {

            1 -> {
                print("Título: ");                        val titulo  = readln()
                print("Autor: ");                         val autor   = readln()
                print("Total de páginas: ");              val paginas = readln().toIntOrNull() ?: 0
                print("Meta págs/dia (Enter = 20): ");    val meta    = readln().toIntOrNull() ?: 20
                sistema.adicionarObra(Livro(titulo, autor, paginas, meta))
            }

            2 -> {
                print("Título: ");                        val titulo  = readln()
                print("Autor: ");                         val autor   = readln()
                print("Duração total (min): ");           val duracao = readln().toIntOrNull() ?: 0
                print("Meta min/dia (Enter = 30): ");     val meta    = readln().toIntOrNull() ?: 30
                sistema.adicionarObra(AudioBook(titulo, autor, duracao, meta))
            }

            3 -> {
                print("Título: ");                        val titulo = readln()
                print("Autor: ");                         val autor  = readln()
                print("Total de capítulos: ");            val caps   = readln().toIntOrNull() ?: 0
                print("Caps/volume (Enter = 10): ");      val cpv    = readln().toIntOrNull() ?: 10
                sistema.adicionarObra(Manga(titulo, autor, caps, cpv))
            }

            4 -> {
                // A busca agora é simplificada e direta pelo ID
                print("ID da obra: ")
                val id = readln().toIntOrNull() ?: run { println("ID inválido."); continue }
                print("Progresso realizado: ")
                val progresso = readln().toIntOrNull() ?: run { println("Inválido."); continue }
                println(sistema.registrarLeitura(id, progresso))
            }

            5 -> {
                print("ID da obra: ")
                val id = readln().toIntOrNull() ?: run { println("ID inválido."); continue }
                print("Nota (1 a 5): ")
                val nota = readln().toIntOrNull() ?: run { println("Inválida."); continue }
                print("Comentário (Enter para pular): ")
                val comentario = readln().takeIf { it.isNotBlank() }
                println(sistema.avaliarObra(id, nota, comentario))
            }

            6  -> sistema.listarObras()
            7  -> sistema.listarObras(ordenarPorProgresso = true)
            8  -> sistema.listarRegistros()
            9  -> sistema.resumo()
            0  -> println("Até logo! 📚")
            else -> println("Opção inválida.")
        }

    } while (opcao != 0)
}