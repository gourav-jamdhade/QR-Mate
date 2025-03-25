<h2>Overview</h2>
<p>QR Mate is a versatile and user-friendly application designed for generating, scanning, and managing QR codes. Built using Kotlin, the app leverages modern tools and frameworks to provide a seamless user experience. It offers robust features such as saving QR codes locally, sharing functionality, and offline deletion management with Firebase integration.</p>

<hr>

<h2>Features</h2>
<ul>
    <li><strong>QR Code Generation</strong>
        <ul>
            <li>Generate QR codes from text, URLs, or custom input.</li>
        </ul>
    </li>
    <li><strong>QR Code Scanning</strong>
        <ul>
            <li>Scan QR codes using the device camera or select an image containing a QR code from the gallery.</li>
        </ul>
    </li>
    <li><strong>Save and Manage QR Codes</strong>
        <ul>
            <li>Save generated QR codes to local storage.</li>
            <li>Integrated with Firebase for cloud storage.</li>
        </ul>
    </li>
    <li><strong>Sharing Options</strong>
        <ul>
            <li>Share QR codes directly via supported sharing options.</li>
        </ul>
    </li>
    <li><strong>Offline Deletion Management</strong>
        <ul>
            <li>Uses Room Database to queue deletions when offline.</li>
            <li>Automatically syncs deletions with Firebase when back online.</li>
        </ul>
    </li>
</ul>

<hr>

<h2>Technology Stack</h2>
<ul>
    <li><strong>Language:</strong> Kotlin</li>
    <li><strong>Architecture:</strong> MVVM</li>
    <li><strong>Database:</strong> Room Database, Firebase Realtime Database</li>
    <li><strong>UI Frameworks:</strong> Material Design Components</li>
    <li><strong>Other Libraries:</strong>
        <ul>
            <li>Picasso/Glide: For image loading</li>
            <li>Retrofit: For network operations</li>
        </ul>
    </li>
</ul>



<h3>Download and Installation</h3>
Clone the repository and import the project into Android Studio. The required dependencies are specified in the build.gradle files.


<h2>Usage</h2>
<ol>
    <li><strong>Home Screen</strong>:
        <ul>
            <li>Choose between generating or scanning a QR code.</li>
        </ul>
    </li>
    <li><strong>Generate QR Code</strong>:
        <ul>
            <li>Enter text or a URL and tap "Generate".</li>
        </ul>
    </li>
    <li><strong>Scan QR Code</strong>:
        <ul>
            <li>Use the camera to scan or upload an image from the gallery.</li>
        </ul>
    </li>
    <li><strong>Manage QR Codes</strong>:
        <ul>
            <li>View saved QR codes, delete them, or share them as needed.</li>
        </ul>
    </li>
</ol>

