
$ErrorActionPreference = "Stop"

$root = Join-Path (Get-Location) "app\src\main\assets\subjects\Mathematics\Grade-10"
New-Item -ItemType Directory -Path $root -Force | Out-Null

function Write-PageIfMissing {
    param(
        [string]$Path,
        [string]$Title,
        [string]$Content
    )

    if (Test-Path -LiteralPath $Path) {
        Write-Host "Skipped existing: $Path" -ForegroundColor Yellow
        return
    }

    $safeTitle = [System.Net.WebUtility]::HtmlEncode($Title)
    $html = @(
        '<!doctype html>'
        '<html lang="en">'
        '<head>'
        '<meta charset="utf-8">'
        '<meta name="viewport" content="width=device-width, initial-scale=1">'
        "<title>$safeTitle</title>"
        '<style>body{font-family:Arial,sans-serif;max-width:850px;margin:auto;padding:20px;line-height:1.7;color:#172033}h1,h2{color:#174ea6}.note{background:#eef5ff;padding:12px;border-radius:8px}.answer{background:#f0fdf4;padding:10px;border-left:4px solid #15803d}li{margin:7px 0}code{background:#f1f5f9;padding:2px 5px}</style>'
        '</head>'
        '<body>'
        "<h1>$safeTitle</h1>"
        $Content
        '</body>'
        '</html>'
    ) -join "`n"

    Set-Content -LiteralPath $Path -Value $html -Encoding UTF8
    Write-Host "Created: $Path" -ForegroundColor Green
}

$units = @(

    @{
        Name = "UNIT-1"
        Title = "Real Numbers and Sets"
        Lessons = @(

            @{
                Title = "Real Number Systems"
                Body = '<p>Real numbers include rational and irrational numbers. Rational numbers can be written as a fraction of integers with a nonzero denominator. Irrational numbers cannot be written as such a fraction.</p><h2>Examples</h2><ul><li>Rational: -3, 0.5, 2/7, 0.333...</li><li>Irrational: square root of 2 and pi.</li></ul><p>Integers include negative whole numbers, zero, and positive whole numbers. Natural numbers are counting numbers, although conventions about including zero vary.</p><h2>Practice</h2><ol><li>Classify 0.75 as rational or irrational.</li><li>Is square root of 9 rational?</li><li>Give one irrational number.</li></ol><details class="answer"><summary>Check answers</summary><p>1. Rational because 0.75 = 3/4. 2. Yes, square root of 9 = 3. 3. Example: square root of 2.</p></details>'
            },
            @{
                Title = "Intervals and Number Line"
                Body = '<p>Intervals describe sets of real numbers. A square bracket means the endpoint is included; a parenthesis means it is excluded.</p><ul><li>[2,5] means 2 &lt;= x &lt;= 5.</li><li>(2,5) means 2 &lt; x &lt; 5.</li><li>[-1,4) means -1 &lt;= x &lt; 4.</li></ul><p>On a number line, use a filled dot for an included endpoint and an open dot for an excluded endpoint.</p><h2>Practice</h2><ol><li>Write the interval for -1 &lt;= x &lt; 4.</li><li>Is 4 included in (1,4)?</li><li>Write x &gt; 3 using interval notation.</li></ol><details class="answer"><summary>Check answers</summary><p>1. [-1,4). 2. No. 3. (3,infinity).</p></details>'
            },
            @{
                Title = "Sets and Set Operations"
                Body = '<p>A set is a collection of distinct elements. The union A union B contains elements in either set. The intersection A intersection B contains elements common to both sets.</p><p>Let A = {1,2,3} and B = {3,4,5}. Their union is {1,2,3,4,5}; their intersection is {3}.</p><p>The empty set has no elements. The complement of a set contains elements in the universal set that are not in that set.</p><h2>Practice</h2><ol><li>Find the union of A={a,b} and B={b,c}.</li><li>Find their intersection.</li><li>What does the empty set mean?</li></ol><details class="answer"><summary>Check answers</summary><p>1. {a,b,c}. 2. {b}. 3. A set with no elements.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-2"
        Title = "Relations and Functions"
        Lessons = @(

            @{
                Title = "Relations and Ordered Pairs"
                Body = '<p>An ordered pair is written (x,y). A relation is a set of ordered pairs. The domain contains first coordinates and the range contains second coordinates.</p><p>For R = {(1,2),(2,4),(3,6)}, the domain is {1,2,3} and range is {2,4,6}.</p><h2>Practice</h2><ol><li>Find the domain of {(0,1),(2,5),(4,9)}.</li><li>Find its range.</li><li>What is an ordered pair?</li></ol><details class="answer"><summary>Check answers</summary><p>1. {0,2,4}. 2. {1,5,9}. 3. A pair in which order matters.</p></details>'
            },
            @{
                Title = "Understanding Functions"
                Body = '<p>A function assigns exactly one output to each allowed input. For example, f(x) = 2x + 1 is a function.</p><h2>Example</h2><p>If f(x) = 2x + 1, then f(3) = 2(3) + 1 = 7.</p><p>If an input is assigned two different outputs, the relation is not a function.</p><h2>Practice</h2><ol><li>Find f(4) if f(x)=x+6.</li><li>Find f(-2) if f(x)=3x.</li><li>Can one input in a function have two different outputs?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 10. 2. -6. 3. No.</p></details>'
            },
            @{
                Title = "Linear Functions and Graphs"
                Body = '<p>A linear function often has the form y = mx + c. The number m is the slope and c is the vertical intercept.</p><p>For y = 2x + 1, points (0,1), (1,3), and (2,5) lie on the line. Plot points and connect them with a straight line.</p><h2>Practice</h2><ol><li>Identify the slope in y = 4x - 3.</li><li>Find y when x = 2 in y = 3x + 2.</li><li>What shape is the graph of a linear function?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 4. 2. 8. 3. A straight line.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-3"
        Title = "Polynomials and Algebra"
        Lessons = @(

            @{
                Title = "Algebraic Expressions and Polynomials"
                Body = '<p>An algebraic expression combines numbers, variables, and operations. A polynomial has terms with nonnegative integer powers of its variables.</p><p>In 3x^2 + 2x - 5, the terms are 3x^2, 2x, and -5. The coefficient of x^2 is 3, and the constant term is -5.</p><h2>Practice</h2><ol><li>How many terms are in 4x^2 - x + 7?</li><li>Evaluate 2x + 3 when x = 4.</li><li>What is the constant term in x^2 + 5x - 8?</li></ol><details class="answer"><summary>Check answers</summary><p>1. Three. 2. 11. 3. -8.</p></details>'
            },
            @{
                Title = "Factorization"
                Body = '<p>Factorization expresses an expression as a product. Look first for a common factor. Special identities can also help.</p><p>Examples: 6x + 12 = 6(x + 2); x^2 - 9 = (x - 3)(x + 3). The second example uses the difference of two squares.</p><h2>Practice</h2><ol><li>Factor 5x + 15.</li><li>Factor x^2 - 16.</li><li>Expand (x + 2)(x + 3).</li></ol><details class="answer"><summary>Check answers</summary><p>1. 5(x + 3). 2. (x - 4)(x + 4). 3. x^2 + 5x + 6.</p></details>'
            },
            @{
                Title = "Algebraic Fractions"
                Body = '<p>An algebraic fraction contains an algebraic expression in its numerator or denominator. A denominator must never equal zero.</p><p>For example, 6x/3 = 2x. Also, (x^2 + 3x)/x = x + 3 when x is not zero.</p><p>Before cancelling factors, factor the numerator and denominator. Never cancel terms that are merely added or subtracted.</p><h2>Practice</h2><ol><li>Simplify 8x/4.</li><li>Simplify (x^2 + 3x)/x, where x is not zero.</li><li>For what value is 1/(x - 2) undefined?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 2x. 2. x + 3. 3. x = 2.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-4"
        Title = "Equations and Inequalities"
        Lessons = @(

            @{
                Title = "Linear Equations"
                Body = '<p>A linear equation has a variable with power one. Perform the same operation on both sides to preserve equality.</p><p>Example: 3x + 4 = 19. Subtract 4 to get 3x = 15, then divide by 3 to get x = 5.</p><h2>Practice</h2><ol><li>Solve x + 7 = 12.</li><li>Solve 4x = 28.</li><li>Solve 2x - 3 = 11.</li></ol><details class="answer"><summary>Check answers</summary><p>1. x = 5. 2. x = 7. 3. x = 7.</p></details>'
            },
            @{
                Title = "Quadratic Equations"
                Body = '<p>A quadratic equation has the form ax^2 + bx + c = 0, where a is not zero. Methods include factorization and the quadratic formula.</p><p>Example: x^2 - 5x + 6 = 0 becomes (x - 2)(x - 3) = 0, so x = 2 or x = 3.</p><h2>Practice</h2><ol><li>Solve x^2 - 9 = 0.</li><li>Factor x^2 + 5x + 6.</li><li>How many distinct real solutions does x^2 = 0 have?</li></ol><details class="answer"><summary>Check answers</summary><p>1. x = -3 or x = 3. 2. (x + 2)(x + 3). 3. One distinct real solution, x = 0.</p></details>'
            },
            @{
                Title = "Linear Inequalities"
                Body = '<p>Inequalities use symbols such as &lt;, &gt;, &lt;=, and &gt;=. Solve them similarly to equations, but reverse the inequality sign when multiplying or dividing both sides by a negative number.</p><p>Example: -2x &gt; 6 gives x &lt; -3.</p><h2>Practice</h2><ol><li>Solve x + 2 &gt; 5.</li><li>Solve 3x &lt;= 12.</li><li>Solve -x &gt; 4.</li></ol><details class="answer"><summary>Check answers</summary><p>1. x &gt; 3. 2. x &lt;= 4. 3. x &lt; -4.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-5"
        Title = "Geometry and Measurement"
        Lessons = @(

            @{
                Title = "Angles and Triangles"
                Body = '<p>Angles on a straight line total 180 degrees. Angles around a point total 360 degrees. The interior angles of a triangle total 180 degrees.</p><p>If a triangle has angles 50 and 60 degrees, its third angle is 180 - 50 - 60 = 70 degrees.</p><h2>Practice</h2><ol><li>Find the third angle of a triangle with angles 35 and 85 degrees.</li><li>Find the supplement of 110 degrees.</li><li>How many degrees are around a point?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 60 degrees. 2. 70 degrees. 3. 360 degrees.</p></details>'
            },
            @{
                Title = "Similarity and Congruence"
                Body = '<p>Congruent shapes have the same size and shape. Similar shapes have the same shape, with corresponding lengths in a constant ratio.</p><p>If corresponding sides of similar triangles are 3 cm and 6 cm, the scale factor from the smaller to the larger is 2. Corresponding angles in similar shapes are equal.</p><h2>Practice</h2><ol><li>What is the scale factor from 4 cm to 12 cm?</li><li>Do congruent triangles have equal corresponding sides?</li><li>Do similar figures necessarily have equal sizes?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 3. 2. Yes. 3. No; they have the same shape but may have different sizes.</p></details>'
            },
            @{
                Title = "Perimeter, Area and Volume"
                Body = '<p>Rectangle area = length times width. Triangle area = one half times base times height. Rectangular prism volume = length times width times height.</p><h2>Examples</h2><ul><li>Rectangle 5 cm by 3 cm: area = 15 square cm.</li><li>Triangle with base 8 cm and height 4 cm: area = 16 square cm.</li><li>Box 2 cm by 3 cm by 4 cm: volume = 24 cubic cm.</li></ul><h2>Practice</h2><ol><li>Find the area of a 7 cm by 4 cm rectangle.</li><li>Find the area of a triangle with base 10 cm and height 6 cm.</li><li>Find the volume of a 5 by 2 by 3 cm box.</li></ol><details class="answer"><summary>Check answers</summary><p>1. 28 square cm. 2. 30 square cm. 3. 30 cubic cm.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-6"
        Title = "Statistics and Probability"
        Lessons = @(

            @{
                Title = "Collecting and Representing Data"
                Body = '<p>Data may be numerical or categorical. Tables, bar charts, line graphs, and histograms help organize and communicate information.</p><p>Label graph axes, include units where appropriate, and choose a scale that represents the data accurately. A bar chart often compares categories; a line graph often shows change over time.</p><h2>Practice</h2><ol><li>Which graph often compares categories?</li><li>Why label graph axes?</li><li>What is the difference between primary and secondary data?</li></ol><details class="answer"><summary>Check answers</summary><p>1. A bar chart. 2. To identify variables and scales. 3. Primary data is collected directly; secondary data was collected by someone else.</p></details>'
            },
            @{
                Title = "Mean, Median, Mode and Range"
                Body = '<p>Mean = sum of values divided by the number of values. Median is the middle value after sorting. Mode is the most frequent value. Range = maximum minus minimum.</p><p>For 2, 3, 3, 6, 11: mean = 5, median = 3, mode = 3, and range = 9.</p><h2>Practice</h2><ol><li>Find the mean of 4, 6, 8.</li><li>Find the median of 1, 5, 7, 9, 10.</li><li>Find the range of 3, 12, 8.</li></ol><details class="answer"><summary>Check answers</summary><p>1. 6. 2. 7. 3. 9.</p></details>'
            },
            @{
                Title = "Basic Probability"
                Body = '<p>For equally likely outcomes, probability = favorable outcomes divided by total possible outcomes. Probability ranges from 0 to 1.</p><p>A fair six-sided die has probability 1/6 of showing a 4 and 3/6 = 1/2 of showing an even number.</p><h2>Practice</h2><ol><li>What is the probability of heads on a fair coin?</li><li>What is the probability of rolling a 7 on a standard die?</li><li>What is the probability of an even result on a fair six-sided die?</li></ol><details class="answer"><summary>Check answers</summary><p>1. 1/2. 2. 0. 3. 1/2.</p></details>'
            }

        )
    },

    @{
        Name = "UNIT-7"
        Title = "Sequences, Patterns and Revision"
        Lessons = @(

            @{
                Title = "Number Patterns and Sequences"
                Body = '<p>A sequence is an ordered list of numbers. An arithmetic sequence has a constant difference between consecutive terms.</p><p>Example: 4, 7, 10, 13 has common difference 3. Its nth term is first term + (n - 1) times the common difference.</p><h2>Practice</h2><ol><li>Find the next term: 2, 5, 8, 11.</li><li>Find the common difference: 12, 8, 4, 0.</li><li>Find the fifth term of 3, 6, 9, ...</li></ol><details class="answer"><summary>Check answers</summary><p>1. 14. 2. -4. 3. 15.</p></details>'
            },
            @{
                Title = "Exponents and Scientific Notation"
                Body = '<p>For nonzero a, a^m times a^n = a^(m+n), and a^m divided by a^n = a^(m-n). Scientific notation expresses a number as a times 10^n, where 1 &lt;= absolute value of a &lt; 10.</p><p>Examples: 45000 = 4.5 times 10^4; 0.0032 = 3.2 times 10^-3.</p><h2>Practice</h2><ol><li>Simplify 2^3 times 2^2.</li><li>Write 720000 in scientific notation.</li><li>Write 0.005 in scientific notation.</li></ol><details class="answer"><summary>Check answers</summary><p>1. 2^5 = 32. 2. 7.2 times 10^5. 3. 5 times 10^-3.</p></details>'
            },
            @{
                Title = "Mathematics Revision Strategies"
                Body = '<p>Good revision involves understanding methods, practising without looking at solutions, checking mistakes, and returning to difficult questions later.</p><ul><li>Write each step clearly.</li><li>Check signs and arithmetic.</li><li>Include units in measurement answers.</li><li>Test a solution by substituting it into the original equation.</li></ul><h2>Practice</h2><ol><li>Solve 5x = 35.</li><li>Factor x^2 - 25.</li><li>Find the mean of 5, 7, 9.</li></ol><details class="answer"><summary>Check answers</summary><p>1. x = 7. 2. (x - 5)(x + 5). 3. 7.</p></details>'
            }

        )
    }

)

$unitLinks = ""

foreach ($unit in $units) {
    $unitPath = Join-Path $root $unit.Name
    New-Item -ItemType Directory -Path $unitPath -Force | Out-Null

    $lessonLinks = ""

    for ($i = 0; $i -lt $unit.Lessons.Count; $i++) {
        $lesson = $unit.Lessons[$i]
        $number = $i + 1
        $lessonFile = "LESSON-$number.html"
        $lessonPath = Join-Path $unitPath $lessonFile

        Write-PageIfMissing -Path $lessonPath -Title $lesson.Title -Content $lesson.Body

        $lessonLinks += "<li><a href='$lessonFile'>$([System.Net.WebUtility]::HtmlEncode($lesson.Title))</a></li>`n"
    }

    $unitContent = @(
        '<p class="note">Supplementary Grade 10 Mathematics practice. Confirm topic coverage with your teacher and current syllabus.</p>'
        '<h2>Lessons</h2>'
        "<ol>$lessonLinks</ol>"
        '<p><a href="UNIT-EXAM.html">Open the unit exam</a></p>'
    ) -join "`n"

    Write-PageIfMissing -Path (Join-Path $unitPath "index.html") -Title $unit.Title -Content $unitContent

    $examContent = @(
        '<p>Show your working before checking the suggested answers.</p>'
        '<ol>'
        '<li>Solve 2x + 5 = 17.</li>'
        '<li>Factor x^2 - 16.</li>'
        '<li>Find the mean of 3, 6, 9.</li>'
        '<li>Find the area of a rectangle 8 cm by 5 cm.</li>'
        '<li>Find the probability of heads on a fair coin.</li>'
        '</ol>'
        '<details class="answer"><summary>Suggested answers</summary><p>1. x = 6. 2. (x - 4)(x + 4). 3. 6. 4. 40 square cm. 5. 1/2.</p></details>'
    ) -join "`n"

    Write-PageIfMissing -Path (Join-Path $unitPath "UNIT-EXAM.html") -Title "$($unit.Title) - Unit Exam" -Content $examContent

    $unitLinks += "<li><a href='$($unit.Name)/index.html'>$([System.Net.WebUtility]::HtmlEncode($unit.Title))</a></li>`n"
}

Write-PageIfMissing -Path (Join-Path $root "index.html") -Title "Grade 10 Mathematics" -Content (
    "<p>Welcome to Learnify's supplementary Grade 10 Mathematics collection.</p><h2>Units</h2><ol>$unitLinks</ol><p>Study lessons, complete unit exams, and then attempt the final exam.</p>"
)

Write-PageIfMissing -Path (Join-Path $root "STUDY-PLAN.html") -Title "Grade 10 Mathematics Study Plan" -Content (
    '<ol><li>Day 1: Real numbers and sets.</li><li>Day 2: Relations and functions.</li><li>Day 3: Polynomials and factorization.</li><li>Day 4: Equations and inequalities.</li><li>Day 5: Geometry and measurement.</li><li>Day 6: Statistics and probability.</li><li>Day 7: Sequences, revision, and final exam.</li></ol><p>One-hour session: 10 minutes review, 30 minutes learning and examples, 15 minutes independent practice, and 5 minutes recording mistakes.</p>'
)

Write-PageIfMissing -Path (Join-Path $root "FORMULA-REFERENCE.html") -Title "Grade 10 Mathematics Formula Reference" -Content (
    '<ul><li>Rectangle area = length * width.</li><li>Triangle area = 0.5 * base * height.</li><li>Rectangular prism volume = length * width * height.</li><li>Mean = sum of values / number of values.</li><li>Range = maximum - minimum.</li><li>Arithmetic sequence nth term = first term + (n - 1) * common difference.</li><li>Quadratic formula: x = (-b +/- sqrt(b*b - 4*a*c)) / (2*a).</li><li>Probability = favorable outcomes / total equally likely outcomes.</li><li>Linear function: y = m*x + c.</li></ul>'
)

Write-PageIfMissing -Path (Join-Path $root "FINAL-EXAM.html") -Title "Grade 10 Mathematics Final Practice Exam" -Content (
    '<ol><li>Solve 3x - 4 = 17.</li><li>Factor x^2 - 7x + 12.</li><li>Find the mean of 4, 6, 8, 10.</li><li>A triangle has angles 45 and 65 degrees. Find the third angle.</li><li>Find the area of a triangle with base 12 cm and height 5 cm.</li><li>Find the probability of rolling an odd number on a fair die.</li><li>Find the next two terms: 5, 9, 13, 17.</li><li>Write 0.00072 in scientific notation.</li><li>If f(x) = 3*x + 2, find f(4).</li><li>Solve x^2 - 4 = 0.</li></ol><details class="answer"><summary>Suggested answers</summary><p>1. x = 7. 2. (x - 3)(x - 4). 3. 7. 4. 70 degrees. 5. 30 square cm. 6. 1/2. 7. 21, 25. 8. 7.2 times 10^-4. 9. 14. 10. x = -2 or x = 2.</p></details>'
)

$htmlFiles = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter "*.html")
$unitFolders = @(Get-ChildItem -LiteralPath $root -Directory | Where-Object { $_.Name -match '^UNIT-[1-7]$' })
$lessonFiles = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter "LESSON-*.html")
$unitExams = @(Get-ChildItem -LiteralPath $root -Recurse -File -Filter "UNIT-EXAM.html")
$unitIndexes = @(Get-ChildItem -LiteralPath $root -Directory | ForEach-Object { Join-Path $_.FullName "index.html" } | Where-Object { Test-Path -LiteralPath $_ })

Write-Host ""
Write-Host "Mathematics content generation and verification" -ForegroundColor Cyan
Write-Host "Total HTML pages: $($htmlFiles.Count)"
Write-Host "Unit folders: $($unitFolders.Count)"
Write-Host "Lesson pages: $($lessonFiles.Count)"
Write-Host "Unit exams: $($unitExams.Count)"
Write-Host "Unit index pages: $($unitIndexes.Count)"

if ($htmlFiles.Count -eq 39 -and $unitFolders.Count -eq 7 -and $lessonFiles.Count -eq 21 -and $unitExams.Count -eq 7 -and $unitIndexes.Count -eq 7) {
    Write-Host "PASS: Expected Mathematics structure verified." -ForegroundColor Green
} else {
    Write-Host "CHECK: Counts differ from expected. Existing files were preserved; inspect the counts above." -ForegroundColor Yellow
}
