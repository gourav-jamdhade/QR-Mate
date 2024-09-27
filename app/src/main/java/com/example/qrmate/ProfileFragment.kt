package com.example.qrmate

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qrmate.DAO.QRDatabase
import com.example.qrmate.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var qrDatabase: QRDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        val account = GoogleSignIn.getLastSignedInAccount(requireActivity())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        qrDatabase = QRDatabase.getDatabase(requireContext())
        val googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        if (account != null) {
            val email = account.email

            binding.tvName.text = "Hi! ${email?.substringBefore("@")}"
        }

        binding.progressBar.visibility = View.VISIBLE
        displayQRCodes(requireContext())
        binding.tvLogout.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseAuth.getInstance().signOut()
                    showLogoutPopUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Logout Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    )
                }
            }


        }
        return binding.root
    }

    private fun showLogoutPopUp() {

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Logout Successful")

        val dialog = builder.create()
        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            val intent = Intent(requireContext(), SignupActivity::class.java)
            startActivity(intent)
        }, 2000)

    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected

    }

    private fun fetchQRCodesFromFirebase(userId: String, onResult: (List<QRCode>) -> Unit) {
        val qrCodesList = mutableListOf<QRCode>()
        val db =
            FirebaseDatabase.getInstance().reference.child("users").child(userId).child("qrcodes")

        db.get().addOnSuccessListener { snapshot ->

            for (qrSnapshot in snapshot.children) {
                val qrCode = qrSnapshot.getValue(QRCode::class.java)
                if (qrCode != null) {
                    qrCodesList.add(qrCode)
                }
            }
            onResult(qrCodesList)
        }.addOnFailureListener {
            onResult(emptyList())
        }
    }

    private fun fetchQRCodeFromLocalStorage(userId: String, onResult: (List<QRCode>) -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {
            val db = QRDatabase.getDatabase(requireContext())
            val localQRCodes = db.qrCodeDao().getAllQRCodes(userId)

            val qrCodes = localQRCodes.map { qrCodeEntity ->
                QRCode(
                    id = qrCodeEntity.id.toString(),
                    name = qrCodeEntity.qrCodeName,
                    imageUrl = qrCodeEntity.qrCodePath
                )
            }

            withContext(Dispatchers.Main) {
                onResult(qrCodes)
            }

        }
    }

    private fun combineAndFilterQRCodes(
        firebaseQRCodes: List<QRCode>,
        localQRCodes: List<QRCode>
    ): List<QRCode> {
        val combinedList = mutableListOf<QRCode>()
        val nameSet = mutableListOf<String>()
        firebaseQRCodes.forEach { qrCode ->
            if (!nameSet.contains(qrCode.name)) {

                combinedList.add(qrCode)
                nameSet.add(qrCode.name)

            }

        }

        localQRCodes.forEach { qrCode ->

            if (!nameSet.contains(qrCode.name)) {
                combinedList.add(qrCode)
                nameSet.add(qrCode.name)
            }

        }

        return combinedList
    }

    private fun displayQRCodes(context: Context){
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if(userId == null){
            binding.tvNoQRCodes.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            return
        }

        if(isInternetAvailable(requireContext())){
            fetchQRCodesFromFirebase(userId){firebaseQRCodes ->
                fetchQRCodeFromLocalStorage(FirebaseAuth.getInstance().currentUser?.email!!.substringBefore("@")) { localQRCodes ->
                    val combinedQRCodes = combineAndFilterQRCodes(firebaseQRCodes, localQRCodes)

                    if(combinedQRCodes.isEmpty()){
                        binding.tvNoQRCodes.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }else{
                        binding.tvNoQRCodes.visibility = View.GONE
                        binding.rvQRCodes.adapter = QRCodeAdapter(combinedQRCodes)
                        binding.rvQRCodes.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }

                }
            }
        }else{
            fetchQRCodeFromLocalStorage(FirebaseAuth.getInstance().currentUser?.email!!.substringBefore("@")) {localQRCodes ->
                if(localQRCodes.isEmpty()){
                    binding.tvNoQRCodes.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }else{
                    binding.tvNoQRCodes.visibility = View.GONE
                    binding.rvQRCodes.adapter = QRCodeAdapter(localQRCodes)
                    binding.rvQRCodes.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }

            }
        }
    }




}