import groovy.json.JsonSlurperClassic

def parseJSON(text) {
  final slurper = new JsonSlurperClassic()
  return new HashMap<>(slurper.parseText(text))
}

String jsontext = new File('assignment_spec.json').getText('UTF-8')
HashMap assignment = parseJSON(jsontext)

println(assignment)
println()
println(assignment['ready'])
println()
println(assignment['testables'])
println()
println(assignment['testables'].size())
println()
println(assignment['testables'][0])
println()
println(assignment['testables'][0]['test_cases'])
println()
println(assignment['testables'][0]['test_cases'].size())

def x = "${assignment['testables'][0]['test_name']}_${assignment['testables'][0]['test_cases'][0]['command']}_output"

println()
println(x)
println(x.replaceAll("[\\W]+", "-"))
