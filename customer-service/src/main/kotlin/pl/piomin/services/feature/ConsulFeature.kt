package pl.piomin.services.feature

import com.orbitz.consul.Consul
import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey

class ConsulFeature(var consulUrl: String) {

    class Config {
        var consulUrl: String = "http://localhost:8500"
        fun build(): ConsulFeature = ConsulFeature(consulUrl)
    }

    companion object Feature : HttpClientPlugin<Config, ConsulFeature> {
        private var currentNodeIndex: Int = 0

        override val key = AttributeKey<ConsulFeature>("ConsulFeature")

        override fun prepare(block: Config.() -> Unit): ConsulFeature = Config().apply(block).build()

        override fun install(feature: ConsulFeature, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) {
                var consulClient = Consul.builder().withUrl(feature.consulUrl).build()
                val nodes = consulClient.healthClient().getHealthyServiceInstances(context.url.host).response
                val selectedNode = nodes[currentNodeIndex]
                context.url.host = selectedNode.service.address
                context.url.port = selectedNode.service.port
                currentNodeIndex = (currentNodeIndex + 1) % nodes.size
                println("Calling ${selectedNode.service.id}: ${context.url.buildString()}")
            }
        }
    }
}