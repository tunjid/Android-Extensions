package com.tunjid.androidx.tablists.tables

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.tunjid.androidx.R
import com.tunjid.androidx.toLiveData
import io.reactivex.processors.PublishProcessor


class StandingsViewModel(application: Application) : AndroidViewModel(application) {

    private val processor = PublishProcessor.create<StandingInput>()

    val state = processor.scan(Standings()) { standings, input ->
        when (input) {
            is StandingInput.Filter -> standings.copy(filter = input.filter)
            is StandingInput.Sort -> standings.copy(sortHeader = standings.sortHeader.copy(
                selectedColumn = input.statHeader.content,
                ascending = input.statHeader.ascending
            ))
        }
    }.toLiveData()

    fun accept(input: StandingInput) = processor.onNext(input)
}

sealed class StandingInput {
    data class Sort(val statHeader: Cell.Header) : StandingInput()
    data class Filter(val filter: GameFilter) : StandingInput()
}

data class Game(val teamA: Team, val teamB: Team) {
    val homeScore = scoreRange.random()
    val awayScore = scoreRange.random()
    val scores = mapOf(teamA to scoreRange.random(), teamB to scoreRange.random())
}

data class Standings(
    override val sortHeader: Cell.Header = Cell.Header(
        content = StatType.Points,
        selectedColumn = StatType.Points,
        ascending = true
    ),
    val filter: GameFilter = GameFilter.All,
    private val table: Map<Team, List<Game>> = simulateTable()
) : Table {

    private val comparator = compareBy<Pair<Team, List<Stat>>> { (_, cells) ->
        val comparison = cells.first { it.type == sortHeader.content }.value
        if (sortHeader.ascending) comparison else -comparison
    }

    override val header = Row.Header(
        content = sortHeader.selectedColumn,
        ascending = sortHeader.ascending,
        cells = standingHeader(
            selectedType = sortHeader.selectedColumn,
            ascending = sortHeader.ascending
        )
    )

    override val rows: List<Row> = listOf(header) + when (filter) {
        GameFilter.All -> table.entries
            .map { (team, games) -> team to games }
        GameFilter.Home -> table.entries
            .map { (team, games) -> team to games.filter(team::isHome) }
        GameFilter.Away -> table.entries
            .map { (team, games) -> team to games.filter(team::isAway) }
    }
        .map(Pair<Team, List<Game>>::row)
        .sortedWith(comparator)
        .mapIndexed { index, (team, stats) ->
            Row.Item(content = team, cells = listOf(
                Cell.Text(content = index.plus(1).toString().toTitledDiffable()),
                Cell.Image(team.badge),
                Cell.Text(
                    content = team,
                    alignment = TextAlignment.Start
                ),
            ) + stats.map(Cell::Text))
        }

    override val sidebar = rows.map {
        when (it) {
            is Row.Item -> it.copy(cells = it.cells.take(2))
            is Row.Header -> it.copy(cells = it.cells.take(2))
        }
    }
}

enum class GameFilter {
    All,
    Home,
    Away
}

enum class Team(val badge: Int) : TitledDiffable {
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

    override val title: CharSequence get() = name
    override val diffId: String get() = name
}

enum class StatType(private val letter: String) : TitledDiffable {
    Points("PTS"),
    Played("P"),
    Wins("W"),
    Draws("D"),
    Losses("L"),
    GoalsFor("GF"),
    GoalsAgainst("GA"),
    GoalDifference("GD");

    override val title: CharSequence get() = letter
    override val diffId: String get() = name
}

private data class Stat(
    val type: StatType,
    val value: Int
) : TitledDiffable {
    override val title: CharSequence get() = value.toString()
    override val diffId: String get() = type.diffId
}

private fun standingHeader(selectedType: TitledDiffable, ascending: Boolean) = listOf(
    Cell.Text(content = "#".toTitledDiffable()),
    Cell.Text(content = "".toTitledDiffable()),
    Cell.Text(content = "Team".toTitledDiffable(), alignment = TextAlignment.Start),
) + StatType.values().map { Cell.Header(content = it, selectedColumn = selectedType, ascending = ascending) }

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

private fun Pair<Team, List<Game>>.row(): Pair<Team, List<Stat>> {
    val (team, games) = this
    val wins = games.count(team::isWinner)
    val draws = games.count(Game::isDraw)
    val goalsFor = games.map { it.scores.getValue(team) }.sum()
    val goalsAgainst = games.map { it.scores.getValue(team.opponent(it)) }.sum()

    return team to listOf(
        Stat(type = StatType.Played, value = games.size),
        Stat(type = StatType.Wins, value = wins),
        Stat(type = StatType.Draws, value = draws),
        Stat(type = StatType.Losses, value = games.count(team::isLoser)),
        Stat(type = StatType.GoalsFor, value = goalsFor),
        Stat(type = StatType.GoalsAgainst, value = goalsAgainst),
        Stat(type = StatType.GoalDifference, value = goalsFor - goalsAgainst),
        Stat(type = StatType.Points, value = wins * 3 + draws * 2),
    )
        .sortedBy(Stat::type)
}

private fun Game.isDraw() = homeScore == awayScore

private fun Team.isHome(game: Game) = this == game.teamA

private fun Team.isAway(game: Game) = this == game.teamB

private fun Team.isWinner(game: Game) = !game.isDraw() && game.scores[this] == game.scores.values.maxOrNull()

private fun Team.isLoser(game: Game) = !game.isDraw() && game.scores[this] == game.scores.values.minOrNull()

private fun Team.opponent(game: Game) = if (this == game.teamA) game.teamB else game.teamA

private val scoreRange = 0..6
