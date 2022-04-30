package automatons

class TokenNFD<StateImpl : State>(
    override val states: MutableList<StateImpl>,
    override val initialState: StateImpl,
    override val transitions: HashMap<String, HashMap<String, StateImpl>>,
    override val finalStates: MutableList<StateImpl>
): NFD<StateImpl>(states, initialState, transitions, finalStates)