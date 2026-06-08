
import kotlin.math.ceil

// Apresenta os detalhes de um objeto
interface Detalhes {
    fun exibirDetalhes(): String
}

// Classe mãe de todas as obras cadastradas no sistema
abstract class Obra(
    val titulo: String,
    val autor: String,
    var metaDiaria: Int
) : Detalhes {

    var id: Int = -1
        private set

    var nota:Int = 1

    var opiniao:String = ""

    internal fun atribuirNota(novaNota:Int){
        nota = novaNota
    }

    internal fun atribuirOpiniao(comentario:String = ""){
        opiniao = comentario
    }

    internal fun atribuirId(novoId: Int) {
        id = novoId
    }

    internal fun getMeta():Int{
        return metaDiaria
    }

    abstract fun editarMeta(novaMeta: Int)

    abstract fun getDuracao():Int

    abstract fun getTotal(): Int

    abstract fun atualizarProgresso(valor: Int): String

    abstract fun tempoRestante(): String

    abstract fun concluido(): Boolean

    abstract fun percentualConcluido(): Double

    abstract fun metaCumprida(progressoHoje: Int): Boolean

    abstract fun diasRestantes(): Int
}

class Livro(
    titulo: String,
    autor: String,
    private val paginasTotal: Int,
    metaDiaria: Int = 10
) : Obra(titulo, autor, metaDiaria) {

    private var paginaAtual: Int = 0

    override fun atualizarProgresso(valor: Int): String {
        if (valor <= 0) {
            return "O número de páginas deve ser maior que zero."
        }
        if (paginaAtual + valor > paginasTotal) {
                return "Esse progresso ultrapassa o total de $paginasTotal páginas."
            }
        paginaAtual += valor
        return "Sucesso"
    }

    override fun getDuracao(): Int {
        return this.paginasTotal
    }

    override fun editarMeta(novaMeta: Int) {
        if (novaMeta > paginasTotal){
            println("Meta de leitura é maior que número de páginas total")
        }
        else{
            metaDiaria = novaMeta
            paginaAtual = 0
        }

    }

    override fun getTotal(): Int {
        return paginasTotal
    }


    override fun tempoRestante(): String = "${paginasTotal - paginaAtual} páginas restantes"

    override fun concluido(): Boolean = paginaAtual == paginasTotal

    override fun percentualConcluido(): Double =
        if (paginasTotal == 0) 0.0
        else (paginaAtual.toDouble() / paginasTotal) * 100

    override fun metaCumprida(progressoHoje: Int): Boolean = progressoHoje >= metaDiaria

    override fun diasRestantes(): Int {
        val restante = paginasTotal - paginaAtual
        return if (metaDiaria == 0) -1 else ceil(restante.toDouble() / metaDiaria).toInt()
    }

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

class AudioBook(
    titulo: String,
    autor: String,
    private val duracaoTotal: Int,
    metaDiaria: Int = 10
) : Obra(titulo, autor, metaDiaria) {

    private var duracaoAtual: Int = 0

    // Polimorfismo: AudioBook resolve progresso em minutos.
    override fun atualizarProgresso(valor: Int): String {
        if (valor <= 0) {
            return "O tempo escutado deve ser maior que zero."
        }
        if (duracaoAtual + valor > duracaoTotal){
            return "Esse progresso ultrapassa a duração total de $duracaoTotal minutos."
        }

        duracaoAtual += valor
        return "Sucesso"
    }

    override fun getDuracao(): Int {
        return this.duracaoTotal
    }

    override fun editarMeta(novaMeta: Int) {
        if (novaMeta > duracaoTotal){
            println("Meta de leitura é maior que a duração em minutos total")
        }
        else{
            metaDiaria = novaMeta
            duracaoAtual = 0
        }
    }

    override fun getTotal(): Int {
        return duracaoTotal
    }

    override fun tempoRestante(): String {
        val diferenca = duracaoTotal - duracaoAtual
        val hora = diferenca/60
        val minuto = diferenca % 60
        return "${hora} hora(s) e ${minuto} minuto(s) restantes"
    }
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


fun barraProgresso(percentual: Double, tamanho: Int = 20): String {
    val preenchido = ((percentual / 100) * tamanho).toInt()
    val vazio = tamanho - preenchido
    return "[" + "█".repeat(preenchido) + "░".repeat(vazio) + "]"
}

// Registra progressos de leitura para as obras cadastradas
class RegistroLeitura(
    val obraId: Int,
    val tipoObra: String,
    val progressoRealizado: Int,
    var notaFinal: Int = 0,
    var comentario: String = "-"
): Detalhes{
    override fun exibirDetalhes(): String {
        return """
        |📌 Registro
        |Obra ID    : $obraId ($tipoObra)
        |Progresso  : +$progressoRealizado
        |Nota final : ${if (notaFinal > 0) notaFinal else "-"}
        |Comentário : ${comentario}
        """.trimMargin()
    }
}


class SistemaLeituras {

    private val obras     = mutableListOf<Obra>() // Lista de obras registradas
    private val registros = mutableListOf<RegistroLeitura>() // Lista de registros de leitura

    private var proximoId = 0 // ID de acompanhamento para nova obra

    // Adiciona nova obra
    fun adicionarObra(obra: Obra) {
        obra.atribuirId(proximoId++)
        val tipo = nomeDoTipo(obra)

        if (obra.getMeta() > obra.getTotal()){
            when (tipo){
                "Livro" -> println("Meta de leitura é maior que número de páginas total")
                "AudioBook" -> println("Meta de leitura é maior que a duração em minutos total")
                else -> "Tipo desconhecido"
            }
        }

        else if (obra.getDuracao() < 0)
            when (tipo){
                "Livro" -> println("Número de páginas inválido")
                "AudioBook" -> println("Duração em minutos inválida")
                else -> "Tipo desconhecido"
            }
        else {
            obras.add(obra)
            println("✅ \"${obra.titulo}\" cadastrada com sucesso! [ID: ${obra.id}]")
        }

    }

    fun getObras(): List<Obra>{
        return this.obras
    }

    // Lista obras registradas, ordenando por progresso ou não
    fun listarObras(ordenarPorProgresso: Boolean = false) {
        if (obras.isEmpty()) { println("Nenhuma obra cadastrada ainda."); return }
        val lista = if (ordenarPorProgresso)
            obras.sortedByDescending { it.percentualConcluido() }
        else
            obras.toList()
        lista.forEach { println("\n${it.exibirDetalhes()}\n" + "─".repeat(45)) }
    }

    // Busca por obras com base no ID
    fun buscarObra(id: Int): Obra? = obras.find { it.id == id }

    // Registra uma nova leitura com base no ID da obra
    fun registrarLeitura(idObra: Int, progresso: Int): String {
        val obra = buscarObra(idObra) ?:  return "❌ Obra com ID $idObra não encontrada."
        val resultado = obra.atualizarProgresso(progresso)
        if (resultado == "Sucesso"){
            registros.add(
                RegistroLeitura(
                    obraId             = idObra,
                    tipoObra           = nomeDoTipo(obra),
                    progressoRealizado = progresso
                )
            )

            val avisoMeta = if (obra.metaCumprida(progresso)) " 🏆 Meta do dia batida!" else " (meta: ${obra.metaDiaria} — faltam ${obra.metaDiaria - progresso})"

            if (obra.concluido()) "🎉 Obra concluída! Não esqueça de avaliar."
            else "✅ ${obra.tempoRestante()}$avisoMeta"
        }
        return resultado
    }

    fun resetarObra(idObra:Int, novaMeta:Int): String{
        val obra = buscarObra(idObra)?:  return "❌ Obra com ID $idObra não encontrada."
        if (novaMeta < 0) return "Nova meta com valor inválido"
        obra.editarMeta(novaMeta)
        return "Sucesso"
    }

    // Avalia obra com base no ID, submentendo uma nota e um comentário (opcional)
    fun avaliarObra(idObra: Int, nota: Int, comentario: String = "-"): String {
        if (nota !in 1..5) return "❌ Nota deve ser entre 1 e 5."
        val obra = buscarObra(idObra) ?: return "❌ Obra com ID $idObra não encontrada."
        if (!obra.concluido()) return "❌ Só posso avaliar obras concluídas."

        obra.atribuirNota(nota)
        obra.atribuirOpiniao(comentario)

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

    // Lista todo o histório de registros para todas as obras
    fun listarRegistros() {
        if (registros.isEmpty()) { println("Nenhum registro ainda."); return }
        registros.forEach { println("\n${it.exibirDetalhes()}\n" + "─".repeat(35)) }
    }

    // Resume as informações sobre obras e registros de leituras
    fun resumo() {
        val total      = obras.size
        val concluidas = obras.count { it.concluido() }
        val notasValidas = obras.filter { it.concluido() }
        val notas      = notasValidas.map { it.nota }
        val media      = if (notas.isEmpty()) "—" else "${"%.1f".format(notas.average())} ⭐"

        println("""
            |===== RESUMO =====
            |Obras cadastradas : $total
            |  📖 Livros       : ${obras.count { it is Livro }}
            |  🎧 Audiobooks   : ${obras.count { it is AudioBook }}
            |Concluídas        : $concluidas
            |Em andamento      : ${total - concluidas}
            |Nota média        : $media
            |Comentários       : ${registros.count { it.comentario != "-" }}
        """.trimMargin())
    }

    // Função auxiliar para determinar tipo de Obra
    private fun nomeDoTipo(obra: Obra): String = when (obra) {
        is Livro     -> "Livro"
        is AudioBook -> "AudioBook"
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
            |─── Leituras ───────────────
            | 3  Registrar Leitura
            | 4  Avaliar Obra Concluída
            | 5  Reler obra
            |─── Consultas ──────────────
            | 6  Listar Obras
            | 7  Listar Obras por Progresso
            | 8  Listar Registros
            | 9  Ver Resumo
            | 0  Sair
        """.trimMargin())

        opcao = readln().toIntOrNull() ?: -1

        when (opcao) {

            1 -> {
                println("Informe os seguintes dados do novo livro:")
                print("Título: ")
                val titulo  = readln()

                print("Autor: ")
                val autor   = readln()

                print("Total de páginas: ")
                val paginas = readln().toIntOrNull() ?: 0

                print("Meta págs/dia (Enter = 10): ")
                val meta    = readln().toIntOrNull() ?: 10

                sistema.adicionarObra(Livro(titulo, autor, paginas, meta))
            }

            2 -> {
                println("Informe os seguintes dados do novo audiobook:")
                print("Título: ")
                val titulo  = readln()

                print("Autor: ")
                val autor   = readln()

                print("Duração total (min): ")
                val duracao = readln().toIntOrNull() ?: 0

                print("Meta min/dia (Enter = 10): ")
                val meta    = readln().toIntOrNull() ?: 10

                sistema.adicionarObra(AudioBook(titulo, autor, duracao, meta))
            }


            3 -> {
                println("Consulte as obras para registro de leitura:")
                sistema.listarObras()
                val obras = sistema.getObras()
                if (obras.isEmpty())
                    continue


                print("ID da obra: ")
                var temp = readln().toIntOrNull()

                if (temp == null) {
                    println("ID inválido.")
                    temp = -1
                    continue
                }

                val id = temp

                print("Informe o progresso realizado: ")
                temp = readln().toIntOrNull()

                if (temp == null) {
                    println("Progresso inválido.")
                    temp = -1
                    continue
                }

                val progresso = temp

                val result = sistema.registrarLeitura(id, progresso)
                if (result == "Sucesso")
                    println("Resgistro de leitura cadastrado!")
                else
                    println(result)
            }

            4 -> {
                val obras = sistema.getObras()
                if (obras.isEmpty()) {
                    println("Nenhuma obra cadastrada ainda.")
                    continue
                }
                println("Consulte a obra para avaliação:")
                print("ID da obra: ")

                var temp = readln().toIntOrNull()

                if (temp == null) {
                    println("ID inválido.")
                    temp = -1
                    continue
                }

                val id = temp
                print("Informe a nota para a obra: ")

                temp = readln().toIntOrNull()

                if (temp == null) {
                    println("ID inválido.")
                    temp = -1
                    continue
                }

                val nota = temp

                print("Comentário (Enter para pular): ")
                var comentario = readln().takeIf { it.isNotBlank() }

                if (comentario == null)
                    comentario = "-"

                println(sistema.avaliarObra(id, nota, comentario))
            }

            5 -> {
                println("Consulte as obras para releitura:")
                sistema.listarObras()
                val obras = sistema.getObras()
                if (obras.isEmpty())
                    continue
                println("Consulte a obra para avaliação:")
                print("ID da obra: ")
                val id    = readln().toIntOrNull() ?: -1

                print("Informe nova meta diária de leitura (Enter = 10): ")
                val meta    = readln().toIntOrNull() ?: 10

                val result = sistema.resetarObra(id, meta)
                if (result != "Sucesso")
                    println(result)
                else
                    println("Pronto. Agora você pode reler seu livro!")
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