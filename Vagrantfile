Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/trusty64"
  config.vm.network :forwarded_port, guest: 25565, host: 25565
  config.vm.provision :shell, path: "bootstrap.sh"
end
