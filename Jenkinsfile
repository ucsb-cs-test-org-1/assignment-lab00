@Library('anacapa-jenkins-lib') import static edu.ucsb.cs.anacapa.pipeline.Lib.*

def assignment = null
def test_results = [:]

def run_test_group(testable) {
  node('submit') {
    /* Try to build the binaries for the current test group */
    def built = true
    def test_cases = testable['test_cases']
    try {
      step([$class: 'WsCleanup'])
      unstash 'fresh'
      sh curtest['build_command']
      stash name: curtest['test_name']
    } catch (e) {
      built = false
      for (int i = 0; i < test_cases.size(); i++) {
        def index = i
        def test_case = test_cases[index]
        def cur_result = [:]
        cur_result['test_group'] = testable['test_name']
        cur_result['test_name'] = test_case['command']
        cur_result['score'] = 0 // fail
        cur_result['max_score'] = test_case['points']
        def jstr = jsonString(cur_result)
        sh "echo '${jstr}' >> .anacapa.tmp_results_${slugify(testable['test_name'])}"
      }
    }

    if (built) {
      for (int i = 0; i < test_cases.size(); i++) {
        def index = i
        run_individual_test_case(testable['test_name'], test_cases[index])
      }
    }

    stash includes: ".anacapa.tmp_results_${slugify(testable['test_name'])}", name: "${slugify(testable['test_name'])}_results"
  }
}

def run_individual_test_case(test_group, test_case) {
  def cur_result = [:]
  def command = test_case['command']
  cur_result['test_group'] = test_group
  cur_result['test_name'] = command
  cur_result['max_score'] = test_case['points']
  try {
    step([$class: 'WsCleanup'])
    unstash name: test_group
    def output_name = "${test_group}_${command}_output"
    output_name = slugify(output_name)
    sh "${command} > ${output_name}"

    if (!test_case['expected'].equalsIgnoreCase('generate')) {
      if (test_case['diff_source'].equalsIgnoreCase('stdout')) {
        def ret = sh returnStatus: true, script: "diff ${output_name} ${test_case['expected']} > ${output_name}.diff"
      } else {
        def ret = sh returnStatus: true, script: "diff ${test_case['diff_source']} ${test_case['expected']} > ${output_name}.diff"
      }
      sh "cat ${output_name}.diff"
      if (ret == 0) {
        cur_result['score'] = test_case['points']
      } else {
        cur_result['score'] = 0
        archiveArtifacts artifacts: "${output_name}.diff", fingerprint: true
      }
    }
  } catch (e) {
    cur_result['score'] = 0 // fail
  } finally {
    def jstr = jsonString(cur_result)
    sh "echo '${jstr}' >> .anacapa.tmp_results_${slugify(testable['test_name'])}"
  }
}


node {
  /* Checkout from Source */
  stage ('Checkout') {
    sh 'ls -al'
    checkout scm
    stash name: 'fresh'
  }

  /* Make sure the assignment spec conforms to the proper conventions */
  stage('CheckJSON') {
    assignment = parseJSON(readFile("assignment_spec.json"))
    // TODO: insert validation step here...
    //    * This allows us to guarantee that the object has certain properties
    if (assignment == null) { sh 'fail' }

    test_results['assignment_name'] = assignment['assignment_name']
    test_results['repo'] = env.JOB_NAME
    test_results['results'] = []
  }

  /* Generate the build stages to run the tests */
  stage('Generate Testing Stages') {
    // def branches = [:]
    /* for each test group */
    def testables = assignment['testables']
    for (int index = 0; index < testables.size(); index++) {
      def i = index
      def curtest = testables[index]
      /* create a parallel group */
      stage(curtest['test_name']) {
        run_test_group(curtest)
      }
    }

    // parallel branches
  }

  stage('Report Results') {
    def testables = assignment['testables']
    for (int index = 0; index < testables.size(); index++) {
      def i = index
      def curtest = testables[index]
      unstash "${curtest['test_name']}_results"
    }
    println(test_results)
    def name = "${env.JOB_NAME}_test_results"
    name = slugify(name)
    def jstr = jsonString(test_results, pretty=true)
    sh "echo '${jstr}' > ${name}.json"
    sh 'ls -al'
    archiveArtifacts artifacts: "${name}.json", fingerprint: true
  }

}
