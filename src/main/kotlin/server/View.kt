package server

import SearchEngineRequestBody
import SearchEngineResponseBody
import actors.SearchEngine
import kotlinx.html.*

fun HTML.renderAggregation(request: SearchEngineRequestBody, result: Map<SearchEngine, SearchEngineResponseBody?>) {
    body {
        div {
            style = "width:60%;align:center;margin: 0 auto; font-family: \"Roboto\", \"Arial\", sans-serif;"
            for ((engine, engineResponse) in result.toSortedMap(compareBy { it.name })) {
                h3 {
                    style = "text-align:center"
                    +engine.name
                }
                engineResponse?.accuracies?.let { accuracies ->
                    if (accuracies.isEmpty()) {
                        p {
                            style = "text-align:center"
                            +"Nothing is found"
                        }
                    }
                    for (accuracy in accuracies) {
                        p {
                            div {
                                b { +accuracy.url }
                                +" (found ${accuracy.found} times)"
                            }
                            +accuracy.splittedSource.first()
                            for (i in 1 until accuracy.splittedSource.size) {
                                b {
                                    style = "color:green"
                                    +request.text
                                }
                                +accuracy.splittedSource[i]
                            }
                            if (accuracy.isTruncated) {
                                +"..."
                            }
                        }
                    }
                } ?: p {
                    style = "text-align:center"
                    +"No response"
                }
            }
        }
    }
}