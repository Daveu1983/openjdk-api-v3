package net.adoptopenjdk.api.v3.dataSources.github.graphql.clients

import io.aexp.nodes.graphql.GraphQLRequestEntity
import net.adoptopenjdk.api.v3.dataSources.github.graphql.models.GHRelease
import net.adoptopenjdk.api.v3.dataSources.github.graphql.models.GHReleaseResult
import net.adoptopenjdk.api.v3.dataSources.models.GithubId
import org.slf4j.LoggerFactory

open class GraphQLGitHubReleaseClient : GraphQLGitHubReleaseRequest() {
    companion object {
        @JvmStatic
        private val LOGGER = LoggerFactory.getLogger(this::class.java)
    }

    suspend fun getReleaseById(id: GithubId): GHRelease {
        val requestEntityBuilder = getReleaseByIdQuery(id)

        LOGGER.info("Getting id $id")

        val result = queryApi(requestEntityBuilder, null, GHReleaseResult::class.java)

        val release: GHRelease
        if (result.response.release.releaseAssets.pageInfo.hasNextPage) {
            release = getNextPage(result.response.release)
        } else {
            release = result.response.release
        }

        return release
    }

    private fun getReleaseByIdQuery(releaseId: GithubId): GraphQLRequestEntity.RequestBuilder {
        return request(
            """query { 
                              node(id:"${releaseId.githubId}") {
                                ... on Release {
                                        id,
                                        url,
                                        name, 
                                        publishedAt,
                                        updatedAt,
                                        isPrerelease,
                                        resourcePath,
                                        releaseAssets(first:50) {
                                            nodes {
                                                downloadCount,
                                                updatedAt,
                                                name,
                                                downloadUrl,
                                                size
                                            },
                                            pageInfo {
                                                hasNextPage,
                                                endCursor
                                            }
                                        }
                                    }
                            }
                            rateLimit {
                                cost,
                                remaining
                            }
                        }
                    """
        )
    }
}
