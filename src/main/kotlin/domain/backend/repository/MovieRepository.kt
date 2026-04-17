package domain.backend.repository

import domain.model.movie.Movie

interface MovieRepository {
    fun findAllMovies(): List<Movie>

    fun findByTitle(title: String): Movie?

    fun saveAll(movies: List<Movie>)
}
