// Checkout github
// When called on the built-in node, we take the sources that have been
// checked out by Jenkins and tar them up in a web-accessible place.
// Worker nodes then just download this tarball and unpack it,
// avoiding multiple git calls.
def call(Map info)
{
    def tarfile = "sources-${env.BUILD_TAG}.tar.gz"
    println("tarfile = ${tarfile}, node=${env.NODE_NAME}")

    if (env.NODE_NAME == 'built-in') {
	shNoTrace("tar --exclude=${tarfile} -czf /var/www/ci.kronosnet.org/buildsources/${tarfile} .",
		  "tar --exclude=${tarfile} -czf <redacted-web-dir>/${tarfile} .")
	info['tarfile'] = tarfile
    } else {
	dir (info['project']) {
	    // This is needed to create the directory before we scp stuff into it
	    shNoTrace('touch .', '')

	    def buildhost = env.NODE_NAME
	    def workspace = env.WORKSPACE

	    // Get the tarball from the Jenkins host
	    // Random delay to stop hitting the server too hard
	    sleep(new Random().nextInt(15))
	    node('built-in') {
		shNoTrace("scp /var/www/ci.kronosnet.org/buildsources/${tarfile} ${buildhost}:${workspace}/${info['project']}",
			  "scp ${tarfile} ${buildhost}:${workspace}/${info['project']}")
	    }
	    sh "tar --no-same-owner -xzf ${tarfile}"
	    sh "rm ${tarfile}"
	}
    }
}
