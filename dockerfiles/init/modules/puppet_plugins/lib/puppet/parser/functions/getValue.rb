module Puppet::Parser::Functions
  newfunction(:getValue, :type => :rvalue) do |args|
    envname = args[0]
    default_value = args[1]
    if ENV[envname].nil?
      actual_value = default_value
    else
      actual_value = ENV[envname]
    end
    return actual_value
  end
end