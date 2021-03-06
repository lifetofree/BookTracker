package com.example.rafae.booktracker.views.Shelf

import android.arch.lifecycle.LifecycleFragment
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.rafae.booktracker.BooksMVP
import com.example.rafae.booktracker.R
import com.example.rafae.booktracker.daggerExample.DaggerApplication
import com.example.rafae.booktracker.models.goodreadpsAPI.responseObjects.Book
import com.example.rafae.booktracker.presenters.ShelfPresenter
import com.example.rafae.booktracker.views.BookAddView
import java.util.*

class ShelfView : LifecycleFragment(), BooksMVP.BooksListViewOps {


//    var applicationContext: Context? = null

    override fun displayBooks(books: ArrayList<Book>) {
        // update recycler view
        val adapter = ShelfViewAdapter(books, context)

        booksList.adapter = adapter
//        booksList.invalidate()
        adapter.notifyDataSetChanged()
    }

    /**
     * Tag for logging.
     */
    protected val TAG = "BooksListView"

    // Responsible to maintain the Objects state
    // during changing configuration
//    private val mStateMaintainer = StateMaintainer(activity.fragmentManager, TAG)

    // Presenter operations
    private var mPresenter: BooksMVP.PresenterOps? = null

    // views
    @BindView(R.id.booksList)
    lateinit var booksList: RecyclerView
    @BindView(R.id.fabAddBook)
    lateinit var addBook: FloatingActionButton

    lateinit var shelf:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // fetch shelf
        shelf = arguments.getString(SHELF)

        // set title
        activity.title = "Currently Reading"

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.books_list, container, false)


        ButterKnife.bind(this, rootView)
        startMVPOps()

        // pass lifecycle
        DaggerApplication.passLifeCycle(this)


        booksList = rootView.findViewById(R.id.booksList)
        booksList.layoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = DividerItemDecoration(booksList.getContext(),
                (booksList.layoutManager as LinearLayoutManager).getOrientation())
        booksList.addItemDecoration(dividerItemDecoration)

        // get books from GoodReads
        mPresenter!!.fetchBooks(shelf)


        return rootView
    }


    /**
     * Handle clicking on button to submit new note.
     *
     * @param v
     */
    @OnClick(R.id.fabAddBook)
    fun addBookClicked(v: View) {

        // go to elapsedTime wehre book is added
        val intent = Intent(activity, BookAddView::class.java)
        this.startActivity(intent)
    }


    /**
     * Initialize and restart the Presenter.
     * This method should be called after { Activity#onCreate(Bundle)}
     */
    fun startMVPOps() {
        try {

            initialize(this)

        } catch (e: Exception) {
            Log.d(TAG, "onCreate() " + e)
            throw RuntimeException(e)
        }

    }


    /**
     * Initialize relevant MVP Objects.
     * Creates a Presenter instance, saves the presenter in [StateMaintainer]
     */
    private fun initialize(view: BooksMVP.BooksListViewOps) {
        mPresenter = ShelfPresenter(view)
//        mStateMaintainer.put("TODO", mPresenter)

    }

    /**
     * Recovers Presenter and informs Presenter that occurred a config change.
     * If Presenter has been lost, recreates a instance
     */
    private fun reinitialize(view: BooksMVP.BooksListViewOps) {
//        mPresenter = mStateMaintainer.get<BooksMVP.PresenterOps>("TODO")

        if (mPresenter == null) {
            Log.w(TAG, "recreating Presenter")
            initialize(view)
        } else {
            mPresenter!!.onConfigurationChanged(view)
        }
    }


    // Show AlertDialog
    override fun showAlert(msg: String) {
        // show alert Box
    }

    // Show Toast
    override fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        // fetch books
        mPresenter!!.fetchBooks(shelf)
    }

    companion object {

        private val ARG_SECTION_NUMBER = "section_number"
        private val SHELF = "shelf"

        /**
         * Different shelves.
         */
        val SHELF_READ = "read"
        val SHELF_TO_READ = "to-read"
        val SHELF_READING = "currently-reading"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(sectionNumber: Int, shelf: String): ShelfView {
            val fragment = ShelfView()
            val args = Bundle()
            args.putInt(ARG_SECTION_NUMBER, sectionNumber)
            args.putString(SHELF, shelf)
            fragment.arguments = args


            return fragment
        }
    }

}