package infra.db.repository

import domain.model.Movie.Movie

interface MovieRepository {
    fun findAllMovies(): List<Movie>

    fun findByTitle(title: String): Movie?

    fun saveAll(movies: List<Movie>)
}
