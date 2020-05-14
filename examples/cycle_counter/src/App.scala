package example.cycle_counter

import cycle._

import com.raquo.laminar.api.L._
import org.scalajs.dom

object Counter {

  case class State(
      value: Int,
      interactions: Int
  )
  val initialState: State = State(0, 0)

  sealed trait Action
  case object Reset     extends Action
  case object Increment extends Action
  case object Decrement extends Action

  def performAction(action: Action, state: State): State = action match {
    case Reset     => state.copy(initialState.value, state.interactions + 1)
    case Increment => state.copy(state.value + 1, state.interactions + 1)
    case Decrement => state.copy(state.value - 1, state.interactions + 1)
  }

  def actionControls(actions: Observer[Action]): Mod[Div] = {
    cycle.amend(
      button(
        cls := "btn secondary",
        "Increment",
        onClick.mapTo(Increment) --> actions
      ),
      button(
        cls := "btn secondary",
        "Decrement",
        onClick.mapTo(Decrement) --> actions
      ),
      button(
        cls := "btn secondary",
        "Reset",
        onClick.mapTo(Reset) --> actions
      )
    )
  }

  def counterView(state: Observable[State]): Div = {
    div(
      h2("Counter value: ", child.text <-- state.map(_.value.toString)),
      h2("Interactions: ", child.text <-- state.map(_.interactions.toString))
    )
  }

  def apply(state: IO[State], actions: IO[Action]): Div = {
    val currentState: Signal[State] = state.startWith(initialState)

    val updatedState: EventStream[State] =
      actions.in.withCurrentValueOf(currentState).map(Function.tupled(performAction))

    div(
      counterView(currentState),
      actionControls(actions.out),
      state.addOut[Div](updatedState)
    )
  }

}

object Main extends App {
  val state: IO[Counter.State]    = new EventBus[Counter.State]
  val actions: IO[Counter.Action] = new EventBus[Counter.Action]
  render(dom.document.getElementById("app"), Counter(state, actions))
}