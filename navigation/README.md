# Navigation

### Navigator

An interface for changing navigation destinations with ```Fragments```.

### StackNavigator

This class enforces rules about ```Fragment```s and the containers they are in a bid to make working with ```Fragment```s a lot easier.
It's fairly opinionated, but it allows for very predictable ```Fragment``` interactions.

The basic crux of it's operation is that every ```Fragment``` added to a container must be added to the ```Fragment``` back stack, with a deterministic tag.

This way, if during navigation, a ```Fragment``` is requested to be shown, and an identical instance of that ```Fragment``` exists,
that same instance is retrieved from the back stack and placed on top of the stack. The most convenient way to generate this tag,
is by implementing the ```TagProvider``` interface, and delegating the ```stableTag``` property to the ```Fragment```'s arguments.

It also helps in preventing the same ```Fragment``` instance being shown twice, one after the other.
If the same ```Fragment``` instance is asked to be shown, the request is simply ignored.

Another benefit is retrieving the current ```Fragment``` in the container, which is extremely useful for shared element transitions.
This is because it allows for the isolation of ```FragmentTransition``` logic from the action that causes or starts them.

If for example clicking a view causes a ```FragmentTransaction``` with a shared element transition, the building of the ```FragmentTransaction``` for the ```Transition``` need not occur
at the click site. In the case of an instance of ```TransactionModifier``` for example, a ```augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment)``` API is exposed,
allowing for the ```Fragment``` leaving to customize the ```FragmentTransaction``` for the ```Fragment``` about to come in, yielding a nice separation of concerns.

Interfaces:

* `TransactionModifier`: A delegate for customizing the FragmentTransaction for Fragments shown and popped of the stack.

* `TagProvider`: Typically implemented by the Fragment's themselves, this is an interface to provide a tag for each Fragment added to the stack.

It also provides:

* A ```transactionModifier``` settable property reference to a `TransactionModifier` for augmenting the ```FragmentTransaction``` that shows each Fragment in the stack.

* A convenience ```currentFragment``` property for accessing the current Fragment on top of the stack.

### MultiStackNavigator

A class that allows for independent stacks of navigation, each backed by a ```StackNavigator``` instance. It is extremely useful for integrating
multiple back stacks with a bottom navigation View. It does this using the child FragmentManager within instances of a ```StackFragment```

It also provides:

* A ```stackSelectedListener``` `((Int) -> Unit)?` callback for when stack selection has changed, either directly or indirectly.

* A ```stackTransactionModifier``` `(FragmentTransaction.(Int) -> Unit)?` for augmenting the ```FragmentTransaction``` that switches between the root ```StackFragment``` for each stack.

* A ```transactionModifier``` `(FragmentTransaction.(Fragment) -> Unit)?` for augmenting the ```FragmentTransaction``` that shows child fragment instances within each ```StackFragment```. This property is delegated internally to the ```StackNavigator``` for each ```StackFragment```.

#### Example usage with BottomNavigationView:

```
class MainActivity : AppCompatActivity(R.layout.activity_main), StackNavigator.NavigationController {

    private val multiStackNavigator: MultiStackNavigator by multiStackNavigator(
            R.id.content_container,
            intArrayOf(R.id.menu_core, R.id.menu_recyclerview, R.id.menu_communications)
    ) { id -> RouteFragment.newInstance(id).let { it to it.stableTag } }

    override val navigator: StackNavigator
        get() = multiStackNavigator.currentNavigator

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<BottomNavigationView>(R.id.bottom_navigation).apply {
            multiStackNavigator.stackSelectedListener = { menu.findItem(it)?.isChecked = true }
            multiStackNavigator.transactionModifier = { incomingFragment ->
                val current = navigator.currentFragment
                if (current is StackNavigator.TransactionModifier) current.augmentTransaction(this, incomingFragment)
                else crossFade()
            }
            multiStackNavigator.stackTransactionModifier = { crossFade() }
            setOnApplyWindowInsetsListener { _: View?, windowInsets: WindowInsets? -> windowInsets }
            setOnNavigationItemSelectedListener { multiStackNavigator.show(it.itemId).let { true } }
            setOnNavigationItemReselectedListener { multiStackNavigator.currentNavigator.clear() }
        }

        onBackPressedDispatcher.addCallback(this) { if (!multiStackNavigator.pop()) finish() }
    }

}
```

Please look at `MainActivity` in the sample app for more detail.
