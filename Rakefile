desc "Run unit test"
task :test => :javac do
  sh 'lein test'
end

desc "Javac"
task :javac do
  sh 'scripts/javac'
end

desc "lein swank"
task :swank => :javac  do
  sh "lein swank"
end

desc "Run application"
task :run => :javac  do
  sh "lein run"
end

desc "pack"
task :pack => :javac  do
  sh "lein uberjar"
end
