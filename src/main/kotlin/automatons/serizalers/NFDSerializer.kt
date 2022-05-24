package automatons.serizalers

import algos.direct.DirectRegexSimplified
import automatons.NFD
import automatons.State
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.antlr.runtime.Token
import org.jetbrains.kotlin.it.unimi.dsi.fastutil.Hash
import tokens.TokenExpression

class NFDSerializer : KSerializer<NFD<DirectRegexSimplified>> {
    private val tokenMapSerializer = MapSerializer(String.serializer(), TokenExpression.serializer())
    private val mapSerializer = MapSerializer(String.serializer(), DirectRegexSimplified.serializer())
    private val mapIdSerializer = MapSerializer(String.serializer(), MapSerializer(String.serializer(), String.serializer()))
    private val finalStatesSerializer = ListSerializer(DirectRegexSimplified.serializer())
    override fun deserialize(decoder: Decoder): NFD<DirectRegexSimplified> {
        return decoder.decodeStructure(descriptor){
            val states: MutableList<DirectRegexSimplified> = mutableListOf()
            val initialState: DirectRegexSimplified
            val transitions: HashMap<String, HashMap<String, DirectRegexSimplified>>
            val finalStates: MutableList<DirectRegexSimplified>
            val transitionTokens: HashMap<String, HashMap<String, TokenExpression>>
            decodeElementIndex(descriptor)
            val statesMap = decodeSerializableElement(
                descriptor,
                0,
                mapSerializer
            )

            states.addAll(statesMap.values)
            decodeElementIndex(descriptor)
            initialState = decodeSerializableElement(
                descriptor,
                1,
                DirectRegexSimplified.serializer()
            )
            decodeElementIndex(descriptor)
            transitions = decodeSerializableElement(
                descriptor,
                2,
                mapIdSerializer
            ).entries.fold(hashMapOf()) { acc, entry ->
                acc[entry.key] = entry.value.entries.fold(hashMapOf()) { accNested, entryNested ->
                    statesMap[entryNested.value]?.let { state ->
                        accNested[entryNested.key] = state
                    }
                    accNested
                }
                acc
            }
            decodeElementIndex(descriptor)
            finalStates = decodeSerializableElement(
                descriptor,
                3,
                finalStatesSerializer
            ).toMutableList()
            decodeElementIndex(descriptor)
            val transitionsTokens = decodeSerializableElement(
                descriptor,
                4,
                mapIdSerializer
            )
            decodeElementIndex(descriptor)
            val tokensMap = decodeSerializableElement(
                descriptor,
                5,
                tokenMapSerializer
            )

            transitionTokens = transitionsTokens.entries.fold(hashMapOf()) { acc, entry ->
                acc[entry.key] = entry.value.entries.fold(hashMapOf()) { accNested, entryNested ->
                    tokensMap[entryNested.value]?.let { token ->
                        accNested[entryNested.key] = token
                    }
                    accNested
                }
                acc
            }


            NFD(
                states,
                initialState,
                transitions,
                finalStates,
                transitionTokens
            )
        }
    }

    override val descriptor: SerialDescriptor
        get() = buildClassSerialDescriptor("NFD") {
            element<HashMap<String, DirectRegexSimplified>>("states")
            element<DirectRegexSimplified>("initialState")
            //Transition, Letter, ID
            element<HashMap<String, HashMap<String, String>>>("transitions")
            element<MutableList<DirectRegexSimplified>>("finalStates")
            //Transition, Letter, ID of token
            element<HashMap<String, HashMap<String, String>>>("transitionTokens")
            element<HashMap<String, TokenExpression>>("tokens")
        }

    override fun serialize(encoder: Encoder, value: NFD<DirectRegexSimplified>) {
        encoder.encodeStructure(descriptor){
            val statesMap = value.states.fold(hashMapOf<String, DirectRegexSimplified>()){ acc, state ->
                acc[state.id] = state
                acc
            }
            this.encodeSerializableElement(
                descriptor,
                0,
                mapSerializer,
                statesMap
            )

            this.encodeSerializableElement(
                descriptor,
                1,
                DirectRegexSimplified.serializer(),
                value.initialState
            )

            val transitions = value.transitions.entries.fold(hashMapOf<String, HashMap<String, String>>()){ acc, entry ->
                acc[entry.key] = entry.value.entries.fold(hashMapOf()){ accNested, nestedEntry ->
                    accNested[nestedEntry.key] = nestedEntry.value.id
                    accNested
                }
                acc
            }

            this.encodeSerializableElement(
                descriptor,
                2,
                mapIdSerializer,
                transitions
            )

            this.encodeSerializableElement(
                descriptor,
                3,
                finalStatesSerializer,
                value.finalStates
            )

            val transitionsTokens = value.transitionTokens.entries.fold(hashMapOf<String, HashMap<String, String>>()){ acc, entry ->
                acc[entry.key] = entry.value.entries.fold(hashMapOf()){ accNested, nestedEntry ->
                    accNested[nestedEntry.key] = nestedEntry.value.ident
                    accNested
                }
                acc
            }

            this.encodeSerializableElement(
                descriptor,
                4,
                mapIdSerializer,
                transitionsTokens
            )

            val tokensMap = value.transitionTokens.flatMap { it.value.values }
                .distinctBy { it.ident }
                .fold(hashMapOf<String, TokenExpression>()){ acc, token ->
                    acc[token.ident] = token
                    acc
            }

            this.encodeSerializableElement(
                descriptor,
                5,
                tokenMapSerializer,
                tokensMap
            )

        }
    }
}