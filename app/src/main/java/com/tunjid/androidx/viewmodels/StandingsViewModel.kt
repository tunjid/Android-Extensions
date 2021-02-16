package com.tunjid.androidx.viewmodels

import android.app.Application
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.recyclerview.diff.Diffable
import com.tunjid.androidx.toLiveData
import io.reactivex.processors.PublishProcessor


class StandingsViewModel(application: Application) : AndroidViewModel(application) {

    private val processor = PublishProcessor.create<StandingInput>()

    val state = processor.scan(Standings()) { standings, input ->
        when (input) {
            is StandingInput.Filter -> standings.copy(filter = input.filter)
            is StandingInput.Sort -> standings.copy(sortHeader = standings.sortHeader.copy(
                selectedType = input.statHeader.type,
                ascending = input.statHeader.ascending
            ))
        }
    }.toLiveData()

    fun accept(input: StandingInput) = processor.onNext(input)
}

sealed class StandingInput {
    data class Sort(val statHeader: Cell.StatHeader) : StandingInput()
    data class Filter(val filter: GameFilter) : StandingInput()
}

data class Game(val teamA: Team, val teamB: Team) {
    val homeScore = scoreRange.random()
    val awayScore = scoreRange.random()
    val scores = mapOf(teamA to scoreRange.random(), teamB to scoreRange.random())
}

data class Standings(
    val sortHeader: Cell.StatHeader = Cell.StatHeader(type = StatType.Points, selectedType = StatType.Points, ascending = true),
    val filter: GameFilter = GameFilter.All,
    private val table: Map<Team, List<Game>> = simulateTable()
) {

    private val comparator = compareBy<Pair<Team, List<Cell.Stat>>> { (_, cells) ->
        val comparison = cells.first { it.type == sortHeader.type }.value
        if (sortHeader.ascending) comparison else -comparison
    }

    val rows: List<Row> = listOf(Row.Header(selectedType = sortHeader.selectedType, ascending = sortHeader.ascending)) + when (filter) {
            GameFilter.All -> table.entries
                .map { (team, games) -> team to games }
                .map(Pair<Team, List<Game>>::row)
            GameFilter.Home -> table.entries
                .map { (team, games) -> team to games.filter(team::isHome) }
                .map(Pair<Team, List<Game>>::row)
            GameFilter.Away -> table.entries
                .map { (team, games) -> team to games.filter(team::isAway) }
                .map(Pair<Team, List<Game>>::row)
        }
            .sortedWith(comparator)
            .mapIndexed { index, (team, stats) ->
                Row.TeamRow(team = team, cells = listOf(
                    Cell.Text(text = index.plus(1).toString()),
                    Cell.Image(team.badge),
                    Cell.Text(text = team.displayName, alignment = TextView.TEXT_ALIGNMENT_TEXT_START),
                ) + stats)
            }

    val sidebar = rows.map {
        when (it) {
            is Row.TeamRow -> it.copy(cells = it.cells.take(2))
            is Row.Header -> it.copy(cells = it.cells.take(2))
        }
    }

    val header = Row.Header(selectedType = sortHeader.selectedType, ascending = sortHeader.ascending)
}

enum class GameFilter {
    All,
    Home,
    Away
}

enum class Team(val badge: Int) {
    Arsenal(R.drawable.arsenal),
    AstonVilla(R.drawable.aston_villa),
    Brighton(R.drawable.brighton),
    Burnley(R.drawable.burnley),
    Chelsea(R.drawable.chelsea),
    CrystalPalace(R.drawable.crystal_palace),
    Everton(R.drawable.everton),
    Fulham(R.drawable.fulham),
    Leeds(R.drawable.leeds),
    Leicster(R.drawable.leicester),
    Liverpool(R.drawable.liverpool),
    ManchesterCity(R.drawable.manchester_city),
    ManchesterUnited(R.drawable.manchester_united),
    Newcastle(R.drawable.newcastle),
    SheffieldUnited(R.drawable.sheffield_united),
    Southampton(R.drawable.southhampton),
    Tottenham(R.drawable.tottenham),
    WestBrom(R.drawable.west_brom),
    WestHam(R.drawable.west_ham),
    Wolves(R.drawable.wolves);

    val displayName get() = this.name
}

enum class StatType(val letter: String) {
    Points("PTS"),
    Played("P"),
    Wins("W"),
    Draws("D"),
    Losses("L"),
    GoalsFor("GF"),
    GoalsAgainst("GA"),
    GoalDifference("GD"),
}

sealed class Row : Diffable {
    abstract val cells: List<Cell>

    data class TeamRow(
        val team: Team,
        override val cells: List<Cell>
    ) : Row()

    data class Header(
        val selectedType: StatType,
        val ascending: Boolean,
        override val cells: List<Cell> = listOf(
            Cell.Text(text = "#"),
            Cell.Text(text = ""),
            Cell.Text(text = "Team", alignment = TextView.TEXT_ALIGNMENT_TEXT_START),
        ) + StatType.values().map { Cell.StatHeader(type = it, selectedType = selectedType, ascending = ascending) }
    ) : Row()

    override val diffId: String
        get() = when (this) {
            is TeamRow -> team.name
            is Header -> "Header"
        }
}

sealed class Cell(val inHeader: Boolean) : Diffable {
    data class Stat(val type: StatType, val value: Int) : Cell(inHeader = false)
    data class Text(val text: CharSequence, val id: String = text.toString(), val alignment: Int = TextView.TEXT_ALIGNMENT_CENTER) : Cell(inHeader = false)
    data class StatHeader(val type: StatType, val selectedType: StatType, val ascending: Boolean = true) : Cell(inHeader = true)
    data class Image(val drawableRes: Int) : Cell(inHeader = false)

    override val diffId: String
        get() = when (this) {
            is Stat -> type.letter
            is Text -> id
            is StatHeader -> type.letter
            is Image -> drawableRes.toString()
        }

    val content
        get() = when (this) {
            is Stat -> value.toString()
            is Text -> text
            is StatHeader -> type.letter
            is Image -> ""
        }

    val textAlignment get() = when(this){
        is Stat -> TextView.TEXT_ALIGNMENT_CENTER
        is Text -> alignment
        is StatHeader -> TextView.TEXT_ALIGNMENT_CENTER
        is Image -> TextView.TEXT_ALIGNMENT_CENTER
    }
}

private fun simulateTable(): Map<Team, List<Game>> =
    Team.values()
        .map { home -> Team.values().filterNot(home::equals).map { away -> home to away } }
        .flatten()
        .map { (a, b) -> Game(a, b) }
        .fold(mutableMapOf()) { map, game ->
            map[game.teamA] = map.getOrPut(game.teamA, ::listOf) + game
            map[game.teamB] = map.getOrPut(game.teamB, ::listOf) + game
            map
        }

private fun Pair<Team, List<Game>>.row(): Pair<Team, List<Cell.Stat>> {
    val (team, games) = this
    val wins = games.count(team::isWinner)
    val draws = games.count(Game::isDraw)
    val goalsFor = games.map { it.scores.getValue(team) }.sum()
    val goalsAgainst = games.map { it.scores.getValue(team.opponent(it)) }.sum()

    return team to listOf(
        Cell.Stat(type = StatType.Played, value = games.size),
        Cell.Stat(type = StatType.Wins, value = wins),
        Cell.Stat(type = StatType.Draws, value = draws),
        Cell.Stat(type = StatType.Losses, value = games.count(team::isLoser)),
        Cell.Stat(type = StatType.GoalsFor, value = goalsFor),
        Cell.Stat(type = StatType.GoalsAgainst, value = goalsAgainst),
        Cell.Stat(type = StatType.GoalDifference, value = goalsFor - goalsAgainst),
        Cell.Stat(type = StatType.Points, value = wins * 3 + draws * 2),
    ).sortedBy(Cell.Stat::type)
}

private fun Game.isDraw() = homeScore == awayScore

private fun Team.isHome(game: Game) = this == game.teamA

private fun Team.isAway(game: Game) = this == game.teamB

private fun Team.isWinner(game: Game) = !game.isDraw() && game.scores[this] == game.scores.values.maxOrNull()

private fun Team.isLoser(game: Game) = !game.isDraw() && game.scores[this] == game.scores.values.minOrNull()

private fun Team.opponent(game: Game) = if (this == game.teamA) game.teamB else game.teamA

private val scoreRange = 0..6
